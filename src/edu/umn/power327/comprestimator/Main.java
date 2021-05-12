package edu.umn.power327.comprestimator;

import edu.umn.power327.comprestimator.files.FileList;

import java.io.File;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Welcome to comprestimator!");
        System.out.println("---------------- \\(^o^)/ ----------------");

        boolean listFiles = false, useTestVector = true, singleFileTest = false;

        // PARSE ARGUMENTS
        for(String arg : args) {
            arg = arg.toLowerCase(Locale.ROOT);
            if (arg.contains("list-files")) {
                listFiles = true;
                System.out.println("Comprestimator will print names of compressed files"
                + "to input_log.txt");
            } else if (arg.contains("single-file")) {
                singleFileTest = true;
                useTestVector = false;
            } else if (arg.contains("help")) {
                usage();
                return;
            } else if (arg.contains("skip-test-vector")) {
                useTestVector = false;
            } else {
                System.out.println("Invalid argument: " + arg);
                usage();
                return;
            }
        }

        CompressorManager cm; // delay instantiation in case this is a single file test

        if (singleFileTest) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the path to some file: ");
            try {
                cm = new CompressorManager(listFiles);
                cm.singleFileTest(new File(scanner.nextLine()));
            } catch (InvalidPathException e) {
                System.out.println("Bad path. Exiting.");
            }
            return;
        }


        // CREATE COMPRESSION MANAGER
        cm = new CompressorManager(listFiles);

        // COMPRESS TEST VECTORS
        if (useTestVector) {
            cm.compressTestVectors();
        }

        cm.beginLoop(); // this is the meat of the operation

        System.out.println("Exited successfully!");
    }

    public static void usage() {
        System.out.println("\tComprestimator usage:\n ------------------------------------");
        System.out.println("Comprestimator uses following compressors by default: deflate (levels 1, 6, 9), lz4, lz4hc, lzma");
        System.out.println("-list-files\t\twill store all file names in input_log.txt");
        System.out.println("-single-file\t\ttest only one file. Will prompt for path.");
    }
}
