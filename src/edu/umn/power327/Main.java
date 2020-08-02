package edu.umn.power327;
import edu.umn.power327.database.DBAdapter;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.zip.*;

public class Main {

    public static void main(String[] args) throws Exception {
	// code goes here omg!
        System.out.println("Welcome to comprestimator!");
        FileEnumerator enumerator = new FileEnumerator();
        // ArrayList should be better than Vector, since ArrayList size grows slower
        // and we don't need this to be thread safe
        System.out.println("Beginning filesystem enumeration...");
        ArrayList<Path> fileList = enumerator.enumerateFiles();
        System.out.println("...enumeration complete.");
        int range = fileList.size(); // will use as a modulus for rng
        int halfSize = range / 2; // this is just for testing to make stuff terminate early

        DBAdapter dbAdapter = new DBAdapter();
        dbAdapter.createTables();
        Random random = new Random();
        Deflater compressor = new Deflater();
        Path curfile;
        String hash;
        byte[] input, output = new byte[1073741824];
        long start, stop, start2, stop2;
        int compressSize, compressSize2;

        while(range > halfSize) {
            // get a random file and swap it with last in fileList (first out-of-range file)
            Collections.swap(fileList, random.nextInt(range), (range - 1));
            range--;
            curfile = fileList.get(range);
            // do compression suite on the first out-of-range file
            // 1) turn file into byte[] and get hash
            try {
                input = Files.readAllBytes(curfile);
//                output = new byte[input.length + 1];
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                hash = stringify(md.digest(input));
            } catch (AccessDeniedException e) {
                continue;
            } catch (IOException e) {
                // probably print something if curfile is null, distinct from
                // when curfile is too large for readAllBytes
                e.printStackTrace();
//                System.out.println("Path: " + curfile.toString());
                continue;
            }
            // 2) compressor.setInput(inputByte[]);
            compressor.setInput(input);
            compressor.finish(); // signals that no new input will enter the buffer
            // 3) TIMER START
            start = System.currentTimeMillis();
            // 4) deflate
            compressSize = compressor.deflate(output);
            // 5) TIMER STOP
            stop = System.currentTimeMillis();
            // 6) reset compressor
            compressor.reset();
            // repeat it
            compressor.setInput(input);
            compressor.finish();
            start2 = System.currentTimeMillis();
            compressSize2 = compressor.deflate(output);
            stop2 = System.currentTimeMillis();
            compressor.reset();

            // store the info in the database
            try {
                dbAdapter.insertResult("deflate_results", hash,
                        "txt", input.length / 1000.0, compressSize / 1000.0, (int)(stop - start) / 1000);

                dbAdapter.insertResult("deflate_results", hash,
                        "txt", input.length / 1000.0, compressSize2 / 1000.0, (int)(stop2 - start2) / 1000);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
//        System.out.println("list size: " + listSize);
    }

    public static String stringify(byte[] digest) {
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
}
