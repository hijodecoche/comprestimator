package edu.umn.power327;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.zip.*;

public class Main {

    public static void main(String[] args) {
	// code goes here omg!
        FileEnumerator enumerator = new FileEnumerator();
        // ArrayList should be better than Vector, since ArrayList size grows slower
        // and we don't need this to be thread safe
        ArrayList<Path> fileList = enumerator.enumerateFiles();
        int range = fileList.size(); // will use as a modulus for rng
        int halfSize = range / 2;

        Random random = new Random();
        Deflater compressor = new Deflater();
        Path curfile;
        String hash;
        byte[] input, output;
        long start, stop;

        while(range > halfSize) {
            // get a random file and swap it with last in fileList (first out-of-range file)
            Collections.swap(fileList, random.nextInt(range), (range - 1));
            range--;
            curfile = fileList.get(range);
            // do compression suite on the first out-of-range file
            // compression things go here
            // 1) turn file into byte[]
            try {
                input = Files.readAllBytes(curfile);
                output = new byte[input.length + 1];
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    hash = Arrays.toString(md.digest(input));
//                    System.out.println(hash);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // probably print something if curfile is null, distinct from
                // when curfile is too large for readAllBytes
                e.printStackTrace();
                continue;
            }
            // 2) TIMER START
            start = System.currentTimeMillis();
            // 3) compressor.setInput(inputByte[]);
            // 4) compressor.deflate(outputByte[]);
            // 5) TIMER STOP
            stop = System.currentTimeMillis();
            // 6) reset compressor
            compressor.reset();

            // store the info in the database
        }
//        System.out.println("list size: " + listSize);
    }
}
