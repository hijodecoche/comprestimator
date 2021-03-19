package edu.umn.power327;

import org.tukaani.xz.*;

import java.io.IOException;

/**
 * This class encapsulates simple LZMA encoding for the purposes of Comprestimator.
 * For simplicity and efficiency, this encoder does not write compressed data to memory.  Instead, it sends data to
 * a NullOutputStream, which counts the number of bytes "written" without doing any writing.
 */
public class XZEncoder {

    private final NullOutputStream nos;
    private XZOutputStream los;
    private final LZMA2Options options = new LZMA2Options();

    /**
     * Create a new XZ compressor at specified level.
     * Valid levels are 0 (minimum compression) to 9 (maximum compression).
     * @param level Compression level between 0 and 9 (inclusive)
     * @throws Exception for unsupported LZMA2Options or IOException when creating OutputStream.
     */
    public XZEncoder(int level) throws Exception {

        nos = new NullOutputStream();

        if (level >= 0 && level < 10) {
            options.setPreset(level);
        }

        los = new XZOutputStream(nos, options, XZ.CHECK_NONE); // skip integrity check
    }

    public XZEncoder() throws Exception {
        this(6);
    }

    public int encode(byte[] input) throws Exception {
        los.write(input, 0, input.length);
        los.close();
        return nos.size();
    }

    public void reset() throws IOException {
        los = new XZOutputStream(nos, options, XZ.CHECK_NONE);
        nos.resetByteCount();
    }
}
