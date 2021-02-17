package edu.umn.power327;

import java.io.File;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
	// code goes here omg!
        System.out.println("Welcome to comprestimator!");
        System.out.println("---------------- \\(^o^)/ ----------------");

        // controls which algorithms to use (expect all)
        boolean useDeflate1 = true, useDeflate6 = true, useDeflate9 = true, useLZ4 = true, useLZ4HC = true,
                useLZMA = true, listFiles = false, useTestVector = true;

        // SINGLE FILE TEST VARS
        boolean singleFileTest = false; // flag for test
        String singleFile; // user input of path name
        Path path; // path to single file


        // PARSE ARGUMENTS
        for(String arg : args) {
            arg = arg.toLowerCase(Locale.ROOT);
            if (arg.contains("list-files")) {
                listFiles = true;
                System.out.println("Comprestimator will print names of compressed files"
                + "to input_log.txt");
            } else if (arg.contains("no-deflate1")) {
//                useDeflate1 = false;
                System.out.println("Not using deflate1");
            } else if (arg.contains("no-deflate6")) {
//                useDeflate6 = false;
                System.out.println("Not using deflate6");
            } else if (arg.contains("no-deflate9")) {
//                useDeflate9 = false;
                System.out.println("Not using deflate9");
            } else if (arg.contains("no-lz4")) {
//                useLZ4 = false;
                System.out.println("Not using LZ4");
            } else if (arg.contains("no-lz4hc")) {
//                useLZ4HC = false;
                System.out.println("Not using LZ4HC");
            } else if (arg.contains("no-lzma")) {
//                useLZMA = false;
                System.out.println("Not using LZMA");
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

        // CREATE COMPRESSION MANAGER
        CompressorManager cm = new CompressorManager(useDeflate1, useDeflate6, useDeflate9, useLZ4,
                useLZ4HC, useLZMA, listFiles);

        FileEnumerator enumerator = new FileEnumerator();

        ArrayList<File> fileList;

        if (!singleFileTest) {
            // ENUMERATE FILES
            System.out.println("Beginning filesystem enumeration...");
            fileList = enumerator.enumerateFiles();
            Collections.shuffle(fileList); // not really necessary unless we expect partial results from a participant
            System.out.println("...enumeration complete.");
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the path to some file: ");
            singleFile = scanner.nextLine();
            try {
                FileSystem fs = FileSystems.getDefault();
                path = fs.getPath(singleFile);
                fileList = new ArrayList<>(1);
                fileList.add(0, path.toFile());
            } catch (InvalidPathException e) {
                System.out.println("Bad path. Exiting.");
                return;
            }
        }
        // not sure if setting enumerator to null will actually destroy the object...
        // ...because fileList might be tied to the enumerator object
        // if that's the case, we need to make fileList an object passed into enumerator during construction
        // or deep copy the list later (which would be inefficient)
        enumerator = null; // we no longer need enumerator, so free up the memory

        cm.setFileList(fileList); // give compression manager the list
        if (useTestVector) {
            cm.compressTestVectors();
        }

        cm.beginLoop(); // this is the meat of the operation

        if (singleFileTest) {
            cm.getResult().printToConsole();
        }

        // DELETE ENUMERATION FILE, IF EXISTS
        try {
            File file = new File("enumeration.dat");
            file.delete(); // throws FileNotFoundException
        } catch (Exception ignored) { }
        System.out.println("Exited successfully!");
    }

    public static void usage() {
        System.out.println("\tComprestimator usage:\n ------------------------------------");
        System.out.println("Comprestimator uses following compressors by default: deflate (levels 1, 6, 9), lz4, lz4hc, lzma");
        System.out.println("-list-files\t\twill store all file names in input_log.txt");
        System.out.println("-single-file\t\ttest only one file. Will prompt for path.");
        System.out.println("-no-deflate1\t\tskips deflate level 1");
        System.out.println("-no-deflate6\t\tskips deflate level 6");
        System.out.println("-no-deflate9\t\tskips deflate level 9");
        System.out.println("-no-lz4\t\tskips lz4");
        System.out.println("-no-lz4hc\t\tskips lz4hc");
        System.out.println("-no-lzma\t\tskips lzma");
    }
}
