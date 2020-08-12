package edu.umn.power327;
import SevenZip.LzmaEncoder;
import edu.umn.power327.database.DBAdapter;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.*;

public class Main {

    public static void main(String[] args) throws Exception {
	// code goes here omg!
        System.out.println("Welcome to comprestimator!");
        System.out.println("---------------- \\(^o^)/ ----------------");
        FileEnumerator enumerator = new FileEnumerator();
        // ArrayList should be better than Vector, since ArrayList size grows slower
        // and we don't need this to be thread safe
        System.out.println("Beginning filesystem enumeration...");
        ArrayList<Path> fileList = enumerator.enumerateFiles();
        Collections.shuffle(fileList);
        System.out.println("...enumeration complete.");

        DBAdapter dbAdapter = new DBAdapter();
        dbAdapter.createTables();
        Robot robot = new Robot();
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        Deflater deflater = new Deflater();
        LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
        LZ4Compressor lz4Compressor = lz4Factory.fastCompressor();
        LzmaEncoder lzmaEncoder = new LzmaEncoder();
        String hash;
        byte[] input, output = new byte[1073741824];
        long start, stop;
        int compressSize;

        for(Path path : fileList) {
            // 1) turn file into byte[] and get sha256 hash
            try {
                input = Files.readAllBytes(path);
                hash = getHash(input);
            } catch (AccessDeniedException e) {
                continue;
            } catch (IOException e) {
                // probably print something if curfile is null, distinct from
                // when curfile is too large for readAllBytes
                e.printStackTrace();
//                System.out.println("Path: " + curfile.toString());
                continue;
            }
            // 2) set compressor input
            deflater.setInput(input);
            deflater.finish(); // signals that no new input will enter the buffer
            // 3) TIMER START
            start = System.currentTimeMillis();
            // 4) deflate
            compressSize = deflater.deflate(output);
            // 5) TIMER STOP
            stop = System.currentTimeMillis();
            // 6) reset compressor
            deflater.reset();

            // store deflate results in the database
            try {
                dbAdapter.insertResult("deflate_results", hash,
                        getExt(path), input.length / 1000.0, compressSize / 1000.0,
                        (int)(stop - start) / 1000);

            } catch (SQLException e) {
                e.printStackTrace();
            }
            robot.mouseMove(mousePoint.x, mousePoint.y);

            start = System.currentTimeMillis();
            compressSize = lz4Compressor.compress(input, output);
            stop = System.currentTimeMillis();
            // store lz4 results
            try {
                dbAdapter.insertResult("lz4_results", hash,
                        getExt(path), input.length / 1000.0, compressSize / 1000.0,
                        (int)(stop - start) / 1000);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            start = System.currentTimeMillis();
            compressSize = lzmaEncoder.encode(input);
            stop = System.currentTimeMillis();
            // store lzma results
            try {
                dbAdapter.insertResult("lzma_results", hash,
                        getExt(path), input.length / 1000.0, compressSize / 1000.0,
                        (int)(stop - start));

            } catch (SQLException e) {
                e.printStackTrace();
            }
            mousePoint = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(mousePoint.x, mousePoint.y);

        }
//        System.out.println("list size: " + listSize);
    }

    public static String getHash(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input);
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getExt(Path path) {
        String s = path.toString();
        if(s.matches("\\.[^\\./\\\\]+$")) {
            s = s.substring(s.lastIndexOf(".") + 1);
            if(s.length() > 8) {
                s = s.substring(0, 7);
            }
            return s;
        }
        else return "";
    }
}
