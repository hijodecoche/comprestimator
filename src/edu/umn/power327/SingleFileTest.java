package edu.umn.power327;

import SevenZip.LzmaEncoder;
import edu.umn.power327.database.DBAdapter;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.awt.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.zip.Deflater;

class SingleFileTest {

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        DBAdapter dbAdapter = new DBAdapter();
        dbAdapter.createTables();
        FileSystem fs = FileSystems.getDefault();
        Path path = null;
        if (args.length != 0) {
            try {
                path = fs.getPath(args[1]);
            } catch (InvalidPathException ignored) { } // gulp
        }

        if (path == null) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the path to some file: ");
            String pathString = scanner.nextLine();
            try {
                path = fs.getPath(pathString);
            } catch (InvalidPathException e) {
                e.printStackTrace();
                System.out.println("Bad path. Exiting.");
                return;
            }
        }
        robot.mouseMove(mousePoint.x, mousePoint.y);
        Deflater deflater = new Deflater();
        LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
        LZ4Compressor lz4Compressor = lz4Factory.fastCompressor();
        LzmaEncoder lzmaEncoder = new LzmaEncoder();
        String hash;
        byte[] input, output = new byte[1073741824];
        long start, stop;
        int compressSize;
        // do compression test here
        input = Files.readAllBytes(path);
        hash = getHash(input);
        // 2) compressor.setInput(inputByte[]);
        deflater.setInput(input);
        deflater.finish();
        // 3) TIMER START
        start = System.currentTimeMillis();
        // 4) deflate
        compressSize = deflater.deflate(output);
        // 5) TIMER STOP
        stop = System.currentTimeMillis();

        // store deflater results
        try {
            dbAdapter.insertResult("deflate_results", hash,
                    getExt(path), input.length / 1000.0, compressSize / 1000.0, (int)(stop - start));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        start = System.currentTimeMillis();
        compressSize = lz4Compressor.compress(input, output);
        stop = System.currentTimeMillis();

        // store lz4
        try {
            dbAdapter.insertResult("lz4_results", hash,
                    getExt(path), input.length / 1000.0, compressSize / 1000.0, (int)(stop - start) / 1000);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        start = System.currentTimeMillis();
        compressSize = lzmaEncoder.encode(input);
        stop = System.currentTimeMillis();

        // store lzma
        try {
            dbAdapter.insertResult("lzma_results", hash,
                    getExt(path), input.length / 1000.0, compressSize / 1000.0, (int)(stop - start));

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            return s.substring(s.lastIndexOf(".") + 1);
        }
        else return "";
    }
}