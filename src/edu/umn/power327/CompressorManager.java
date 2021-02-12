package edu.umn.power327;

import SevenZip.LzmaEncoder;
import edu.umn.power327.database.DBController;
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
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.*;

/**
 * Encapsulates all compressors and their uses.
 * TODO: Utilize the partial compressor list; currently the only way to run compressors is to use all, not some.
 * TODO: To preserve speed for the common case (use all compressors), consider breaking the for loop section into one
 * TODO: of two functions: loopAll or loopPartial, where loopPartial checks if (useLZMA) before compressing, etc.
 */
public class CompressorManager {

    // compressors
    private Deflater deflater1, deflater6, deflater9;
    private LZ4Compressor lz4Compressor;
    private LZ4Compressor lz4hc;
    private LzmaEncoder lzmaEncoder;

    private final CompressionResult result = new CompressionResult();
    private ArrayList<File> fileList;
    private byte[] input;
    private final byte[] output = new byte[1610612736]; // 1.5 GB
    private long start, stop;
    private boolean list_files = false;
    private DBController dbController;
    private Robot robot; // will be instantiated if not headless env

    /**
     * Convenience method, used if we want to use all compression algorithms regardless of user input.
     * @throws Exception from LZMAEncoder, Robot, DBController, etc.
     */
    public CompressorManager() throws Exception {
        new CompressorManager(true, true, true, true, true, true,
                false, true);
    }

    public CompressorManager(boolean useDeflate1, boolean useDeflate6, boolean useDeflate9, boolean useLZ4,
                             boolean useLZ4HC, boolean useLZMA, boolean list_files, boolean useTestVector) throws Exception {
        this.list_files = list_files;
        if (useDeflate1) deflater1 = new Deflater(1);
        if (useDeflate6) deflater6 = new Deflater();
        if (useDeflate9) deflater9 = new Deflater(9);
        LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
        if (useLZ4) lz4Compressor = lz4Factory.fastCompressor();
        if (useLZ4HC) lz4hc = lz4Factory.highCompressor();
        if (useLZMA) lzmaEncoder = new LzmaEncoder();

        if (!GraphicsEnvironment.isHeadless()) {
            robot = new Robot(); // hacky way to keep computer awake
        } else {
            System.out.println("!!! Java has no graphics access!");
            System.out.println("Please make sure your computer will not fall asleep when idle!");
            System.out.println("Check README if you need help.\n\t------------------------------");
        }

        dbController = new DBController();
        dbController.createTables();

        if (useTestVector) {
            compressTestVector();
        }
    }

    public void beginLoop() throws Exception {

        Point mousePoint; // never instantiated when in headless env
        // using singleFileTest will be faster than multiple calls to fileList.size()
        boolean singleFileTest = fileList.size() == 1;
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
                    + "to input_log.txt");
        }

        System.out.println("Beginning compression loop...");
        for(File file : fileList) {
            try {
                if (fw != null) {
                    fw.write(file.getPath() + "\n");
                    fw.flush();
                }
                // weed out pseudo-files
                if (!file.isFile()) {
                    continue;
                }
                // fetch file type, if filesystem has sh
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
                    if (singleFileTest) {
                        dbController.deleteFromAll(result.getHash(), result.getOrigSize());
                    }
                    else
                        continue;
                }


                ///////////////////////////////////////////////////////
                // BEGIN DEFLATE
                // at level 1
                doDeflate(deflater1);
                dbController.insertResult("deflate1_results", result);

                // level 6
                doDeflate(deflater6);
                dbController.insertResult("deflate6_results", result);

                // level 9
                doDeflate(deflater9);
                dbController.insertResult("deflate9_results", result);

                // END DEFLATE ////////////////////////////////////////

                if (robot != null) {
                    mousePoint = MouseInfo.getPointerInfo().getLocation();
                    robot.mouseMove(mousePoint.x, mousePoint.y);
                }

                ///////////////////////////////////////////////////////
                // BEGIN LZ4
                try {

                    doLZ4();
                    dbController.insertResult("lz4_results", result);
                    doLZ4HC();
                    dbController.insertResult("lz4hc_results", result);

                } catch (LZ4Exception e) {
                    System.out.println("LZ4Exception caught: ");
                    System.out.println(file.getPath());
                }
                // END LZ4 ////////////////////////////////////////////

                ///////////////////////////////////////////////////////
                // BEGIN LZMA
                doLZMA();
                dbController.insertResult("lzma_results", result);
                // END LZMA

                if (robot != null) {
                    mousePoint = MouseInfo.getPointerInfo().getLocation();
                    robot.mouseMove(mousePoint.x, mousePoint.y);
                }
            } catch (OutOfMemoryError e) {
                System.out.println(" --- OOM Error caught:");
                System.out.println(file.getPath());
                System.out.println("Continuing compression loop...");
            } catch (SQLException e) {
                // this almost certainly means the file command isn't working
                // set it to null to skip future file attempts
                fetcher = null;
            } catch (IOException ignored) {
                // catches AccessDenied and FileNotFound
                // continue;
            }

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

    public static String getExt(String path) {
        if(path.matches(".*\\.[A-Za-z0-9]+$")) {
            int index = path.lastIndexOf('.');
            if(index > 0 && path.charAt(index - 1) != '\\' && path.charAt(index - 1) != '/') {
                return path.substring(path.lastIndexOf(".") + 1);
            }
        }
        return "";
    }

    private void compressTestVector() throws Exception {
        // COMPRESS TEST VECTOR
        if (!dbController.contains("0", 1)) {
            System.out.println("Compressing test vector...");
            Random random = new Random(65536); // hardcoded seed makes RNG generate identical test vectors
            input = new byte[268435456]; // 256 mb
            random.nextBytes(input);
            result.setHash("0000000000000000000000000000000000000000000000000000000000000000");
            result.setExt("");
            result.setOrigSize(1);
            // BEGIN DEFLATE
            // at level 1
            doDeflate(deflater1);
            dbController.insertResult("deflate1_results", result);

            // level 6
            doDeflate(deflater6);
            dbController.insertResult("deflate6_results", result);

            // level 9
            doDeflate(deflater9);
            dbController.insertResult("deflate9_results", result);

            doLZ4();
            dbController.insertResult("lz4_results", result);

            doLZ4HC();
            dbController.insertResult("lz4hc_results", result);

            doLZMA();
            dbController.insertResult("lzma_results", result);

            System.out.println("Done compressing test vector.");
        }
    }

    public void setFileList(ArrayList<File> fileList) {
        // assumes fileList is already shuffled
        this.fileList = fileList;
    }

    public CompressionResult getResult() {
        return result;
    }

    ///////////////////////////////////////////
    // Functions for individual compressors: //
    ///////////////////////////////////////////

    private void doDeflate(Deflater deflater) throws SQLException {
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

    private void doLZ4() throws SQLException {
        start = System.nanoTime();
        result.setCompressSize(lz4Compressor.compress(input, output));
        stop = System.nanoTime();
        result.setCompressTime((stop - start) / 1000);
    }

    private void doLZ4HC() throws SQLException {
        // LZ4HC
        start = System.nanoTime();
        result.setCompressSize(lz4hc.compress(input, output));
        stop = System.nanoTime();
        result.setCompressTime((stop - start) / 1000);
    }

    private void doLZMA() throws Exception {
        start = System.nanoTime();
        result.setCompressSize(lzmaEncoder.encode(input));
        stop = System.nanoTime();
        result.setCompressTime((stop - start) / 1000);
        lzmaEncoder.reset();
    }
}
