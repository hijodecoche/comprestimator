package edu.umn.power327;
import SevenZip.LzmaEncoder;
import edu.umn.power327.database.DBController;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;

import java.awt.*;
import java.io.FileWriter;
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

        Robot robot = null; // will be instantiated if not headless env
        Point mousePoint = null;
        if (!GraphicsEnvironment.isHeadless()) {
            robot = new Robot(); // hacky way to keep computer awake
            mousePoint = MouseInfo.getPointerInfo().getLocation();
        } else {
            System.out.println("!!! Java has no graphics access!");
            System.out.println("Please make sure your computer will not fall asleep when idle!");
            System.out.println("Check README if you need help.");
        }

        FileEnumerator enumerator = new FileEnumerator();
        System.out.println("Beginning filesystem enumeration...");
        ArrayList<Path> fileList = enumerator.enumerateFiles();
        Collections.shuffle(fileList);
        System.out.println("...enumeration complete.");

        DBController dbController = new DBController();
        dbController.createTables();

        // initialize compressors
        Deflater deflater1 = new Deflater(1), deflater6 = new Deflater(), deflater9 = new Deflater(9);
        LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
        LZ4Compressor lz4Compressor = lz4Factory.fastCompressor();
        LZ4Compressor lz4hc = lz4Factory.highCompressor();
        LzmaEncoder lzmaEncoder = new LzmaEncoder();

        // initialize results variables
        CompressionResult result = new CompressionResult();
        byte[] input, output = new byte[1610612736]; // 1.5 GB
        long start, stop;

        // File writer
        FileWriter fw = new FileWriter("input_log.txt");

        System.out.println("Beginning compression loop...");
        for(Path path : fileList) {
            // turn file into byte[] and get sha256 hash
            try {
                fw.write(path.toString() + "\n");
                input = Files.readAllBytes(path);
                result.setOrigSize(input.length);
                result.setHash(getHash(input));
                result.setExt(getExt(path));
                // check if we've seen this file before
                if (dbController.contains(result.getHash(), result.getOrigSize()))
                    continue;
            } catch (IOException e) {
                // catches FileNotFound and AccessDenied
                continue;
            } catch (OutOfMemoryError e) {
                System.out.println(" --- OOM Error caught:");
                System.out.println(path.toString());
                System.out.println("Continuing compression loop...");
                continue;
            }

            ///////////////////////////////////////////////////////
            // BEGIN DEFLATE
            // at level 1
            deflater1.setInput(input);
            deflater1.finish(); // signals that no new input will enter the buffer
            start = System.nanoTime(); // start timer
            result.setCompressSize(deflater1.deflate(output));
            stop = System.nanoTime(); // stop timer
            result.setCompressTime((stop - start) / 1000);

            // store deflate results in the database
            try {
                dbController.insertResult("deflate1_results", result, path.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // level 6
            deflater6.setInput(input);
            deflater6.finish(); // signals that no new input will enter the buffer
            start = System.nanoTime(); // start timer
            result.setCompressSize(deflater6.deflate(output));
            stop = System.nanoTime(); // stop timer
            result.setCompressTime((stop - start) / 1000);

            // store deflate1 results in the database
            try {
                dbController.insertResult("deflate6_results", result, path.toString());

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // level 9
            deflater9.setInput(input);
            deflater9.finish(); // signals that no new input will enter the buffer
            start = System.nanoTime(); // start timer
            result.setCompressSize(deflater9.deflate(output));
            stop = System.nanoTime(); // stop timer
            result.setCompressTime((stop - start) / 1000);

            // store deflate9 results in the database
            try {
                dbController.insertResult("deflate9_results",result, path.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (robot != null) {
                robot.mouseMove(mousePoint.x, mousePoint.y); // keep computer awake
            }
            // END DEFLATE ////////////////////////////////////////

            ///////////////////////////////////////////////////////
            // BEGIN LZ4
            // at standard compression
            try {
                start = System.nanoTime();
                result.setCompressSize(lz4Compressor.compress(input, output));
                stop = System.nanoTime();
                result.setCompressTime((stop - start) / 1000);
                // store lz4 results
                try {
                    dbController.insertResult("lz4_results", result, path.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // LZ4HC
                start = System.nanoTime();
                result.setCompressSize(lz4hc.compress(input, output));
                stop = System.nanoTime();
                result.setCompressTime((stop - start) / 1000);
                // store lz4 results
                try {
                    dbController.insertResult("lz4hc_results", result, path.toString());

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (LZ4Exception e) {
                System.out.println("LZ4Exception caught: ");
                System.out.println(path.toString());
            }

            // END LZ4

            ///////////////////////////////////////////////////////
            // BEGIN LZMA
            start = System.nanoTime();
            result.setCompressSize(lzmaEncoder.encode(input));
            stop = System.nanoTime();
            result.setCompressTime((stop - start) / 1000);
            lzmaEncoder.reset();
            // store lzma results
            try {
                dbController.insertResult("lzma_results", result, path.toString());

            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (robot != null) {
                mousePoint = MouseInfo.getPointerInfo().getLocation();
                robot.mouseMove(mousePoint.x, mousePoint.y);
            }
            // END LZMA

        } // END FOR-LOOP
        System.out.println("Exited successfully!");
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
        if(s.matches(".*\\.[A-Za-z0-9]+$")) {
            int index = s.lastIndexOf('.');
            if(index > 0 && s.charAt(index - 1) != '\\' && s.charAt(index - 1) != '/') {
                return s.substring(s.lastIndexOf(".") + 1);
            }
        }
        return "";
    }
}
