package edu.umn.power327;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws Exception {
	// code goes here omg!
        System.out.println("Welcome to comprestimator!");
        System.out.println("---------------- \\(^o^)/ ----------------");

        // controls which algorithms to use (expect all)
        boolean useDeflate1 = true, useDeflate6 = true, useDeflate9 = true, useLZ4 = true, useLZ4HC = true,
                useLZMA = true, list_files = false;


        // PARSE ARGUMENTS
        for(String arg : args) {
            arg = arg.toLowerCase(Locale.ROOT);
            if (arg.contains("list-files ")) {
                list_files = true;
                System.out.println("Comprestimator will print names of compressed files"
                + "to input_log.txt");
            } else if (arg.contains("no-deflate1 ")) {
                useDeflate1 = false;
                System.out.println("Not using deflate1");
            } else if (arg.contains("no-deflate6 ")) {
                useDeflate6 = false;
                System.out.println("Not using deflate6");
            } else if (arg.contains("no-deflate9 ")) {
                useDeflate9 = false;
                System.out.println("Not using deflate9");
            } else if (arg.contains("no-lz4 ")) {
                useLZ4 = false;
                System.out.println("Not using LZ4");
            } else if (arg.contains("no-lz4hc ")) {
                useLZ4HC = false;
                System.out.println("Not using LZ4HC");
            } else if (arg.contains("no-lzma ")) {
                useLZMA = false;
                System.out.println("Not using LZMA");
            }
        }

        // CREATE COMPRESSION MANAGER
        CompressorManager cm = new CompressorManager(useDeflate1, useDeflate6, useDeflate9, useLZ4, useLZ4HC, useLZMA, list_files);

        // Enumerate files
        FileEnumerator enumerator = new FileEnumerator();

        System.out.println("Beginning filesystem enumeration...");

        ArrayList<Path> fileList = enumerator.enumerateFiles();
        Collections.shuffle(fileList); // not really necessary unless we expect partial results from a participant
        cm.setFileList(fileList); // give compression manager the list

        System.out.println("...enumeration complete.");
        System.out.println("Beginning compression loop...");

        cm.beginLoop(); // this is where the magic happens

        System.out.println("Exited successfully!");
    }
}
