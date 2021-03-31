package edu.umn.power327;

import edu.umn.power327.database.DBController;
import edu.umn.power327.files.*;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Random;
import java.util.zip.*;

/**
 * Encapsulates all compressors and their uses.
 * TODO: Utilize the partial compressor list; currently the only way to run compressors is to use all, not some.
 * TODO: Remove e.printStackTrace() before sending to other people!
 */
public class CompressorManager {

    // compressors
    private final Deflater deflater1;
    private final Deflater deflater6;
    private final Deflater deflater9;
    private final LZ4Compressor lz4;
    private final LZ4Compressor lz4hc;
    private final XZEncoder xz6;
    private final XZEncoder xz9;

    private final CompressionResult result = new CompressionResult();
    private FileList fileList;
    private byte[] input;
    private final byte[] output = new byte[1610612736]; // 1.5 GB
    private long start, stop;
    private final boolean list_files;
    private final DBController dbController = DBController.getInstance();
    private Robot robot; // will be instantiated if not headless env

    public CompressorManager(boolean list_files) throws Exception {
        this.list_files = list_files;
        deflater1 = new Deflater(1);
        deflater6 = new Deflater();
        deflater9 = new Deflater(9);
        LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
        lz4 = lz4Factory.fastCompressor();
        lz4hc = lz4Factory.highCompressor();
        xz6 = new XZEncoder();
        xz9 = new XZEncoder(9);

        if (!GraphicsEnvironment.isHeadless()) {
            robot = new Robot(); // hacky way to keep computer awake
        } else {
            System.out.println("!!! Java has no graphics access!");
            System.out.println("Please make sure your computer will not fall asleep when idle!");
            System.out.println("Check README if you need help.\n\t------------------------------");
        }

        if (dbController == null)
            throw new Exception("Could not get database instance!");
    }

    private void compressAndStoreAll() throws Exception {

        Point mousePoint; // never instantiated when in headless env

        doDeflate(deflater1);
        dbController.insertDeflate1(result);

        doDeflate(deflater6);
        dbController.insertDeflate6(result);

        doDeflate(deflater9);
        dbController.insertDeflate9(result);

        if (robot != null) {
            mousePoint = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(mousePoint.x, mousePoint.y);
        }

        doLZ4(lz4);
        dbController.insertLZ4(result);

        doLZ4(lz4hc);
        dbController.insertLZ4HC(result);

        doLZMA(xz6);
        dbController.insertXZ6(result);

        doLZMA(xz9);
        dbController.insertXZ9(result);

        if (robot != null) {
            mousePoint = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(mousePoint.x, mousePoint.y);
        }
    }

    public void singleFileTest(File file) {
        FileTypeFetcher fetcher = new FileTypeFetcher();
        try {
            result.setType(fetcher.fetchType(file.toString()));
        } catch (Exception ignored) {}
        try {
            input = Files.readAllBytes(file.toPath());
            result.setOrigSize(input.length);
            result.setHash(getHash(input));
            result.setExt(getExt(file.getPath()));

            doDeflate(deflater1);
            result.printToConsole();

            doDeflate(deflater6);
            result.printToConsole();

            doDeflate(deflater9);
            result.printToConsole();

            doLZ4(lz4);
            result.printToConsole();

            doLZ4(lz4hc);
            result.printToConsole();

            doLZMA(xz6);
            result.printToConsole();

            doLZMA(xz9);
            result.printToConsole();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beginLoop() throws Exception {

        FileWriter fw = null;

        FileTypeFetcher fetcher = new FileTypeFetcher();
        try {
            String s = fetcher.fetchType("comprestimator.jar");
        } catch (IOException e) {
            System.out.println("Will not use unix file command.");
            fetcher = null;
        }

        if (list_files) {
            fw = new FileWriter("input_log.txt");
            System.out.println("Comprestimator will print names of compressed files"
                    + " to input_log.txt");
        }

        System.out.println("Beginning compression loop...");
        File file;
        while((file = fileList.getNext()) != null) {
            try {
                if (fw != null) {
                    fw.write(file.getPath() + "\n");
                    fw.flush();
                }

                if (!file.isFile() || file.length() > 1073741824) {
                    // file is pseudo-file or file is larger than 1 GB
                    continue;
                }
                // fetch file type, if available
                if (fetcher != null) {
                    result.setType(fetcher.fetchType(file.getPath()));
                }
                // turn file into byte[] and get metadata
                input = Files.readAllBytes(file.toPath());
                result.setOrigSize(input.length);
                result.setHash(getHash(input));
                result.setExt(getExt(file.getPath()));
                // check if we've seen this file before
                if (dbController.contains(result.getHash(), result.getOrigSize())) {
                    continue;
                }

                compressAndStoreAll();

            } catch (SQLException e) {
                // this almost certainly means the file command isn't working
                // set it to null to skip future file attempts
                e.printStackTrace();
                fetcher = null;
            } catch (LZ4Exception e) {
                // these are strange and rare, so we want to know what's going on
                System.out.println("LZ4Exception caught: ");
                System.out.println(file.getPath());
            } catch (OutOfMemoryError | IOException ignored) { }

        } // END COMPRESSION LOOP
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

    public static String getExt(String pathname) {
        if(pathname.matches(".*\\.[A-Za-z0-9]+$")) {
            int index = pathname.lastIndexOf('.');
            if(index > 0 && pathname.charAt(index - 1) != '\\' && pathname.charAt(index - 1) != '/') {
                return pathname.substring(pathname.lastIndexOf(".") + 1);
            }
        }
        return "";
    }

    public void compressTestVectors() throws Exception {
        // COMPRESS ZERO VECTOR
        if (!dbController.contains("0", 1)) {
            System.out.println("Compressing zero vector...");
            input = new byte[268435456]; // 256 mb, auto initialized to zeros
            result.setHash("0");
            result.setExt("");
            result.setOrigSize(1);

            compressAndStoreAll();

            System.out.println("Done compressing zero vector.");

            // COMPRESS RANDOM VECTOR
            System.out.println("Compressing random test vector...");
            Random random = new Random(65536); // hardcoded seed makes RNG generate same data
            random.nextBytes(input);
            result.setHash("1"); // Ext and OrigSize already set

            compressAndStoreAll();

            System.out.println("Done compressing random vector.");
        }
    }

    public void setFileList(FileList fileList) {
        // assumes fileList is already shuffled
        this.fileList = fileList;
    }

    public CompressionResult getResult() {
        return result;
    }

    ///////////////////////////////////////////
    // Functions for individual compressors: //
    ///////////////////////////////////////////

    private void doDeflate(Deflater deflater) {
        deflater.setInput(input);
        deflater.finish(); // signals that no new input will enter the buffer
        int byteCount = 0;

        start = System.nanoTime(); // start timer
        while (!deflater.finished()) {
            byteCount += deflater.deflate(output);
        }
        stop = System.nanoTime(); // stop timer
        deflater.reset();

        result.setCompressSize(byteCount);
        result.setCompressTime((stop - start) / 1000);
    }

    private void doLZ4(LZ4Compressor lz4Compressor) {
        start = System.nanoTime();
        result.setCompressSize(lz4Compressor.compress(input, output));
        stop = System.nanoTime();
        result.setCompressTime((stop - start) / 1000);
    }

    private void doLZMA(XZEncoder xzEncoder) throws Exception {
        start = System.nanoTime();
        result.setCompressSize(xzEncoder.encode(input));
        stop = System.nanoTime();
        result.setCompressTime((stop - start) / 1000);
        xzEncoder.reset();
    }
}
