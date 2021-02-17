package edu.umn.power327;

import org.tukaani.xz.*;

/**
 * This class encapsulates simple LZMA encoding for the purposes of Comprestimator.
 * For simplicity and efficiency, this encoder does not write compressed data to memory.  Instead, it sends data to
 * a NullOutputStream, which counts the number of bytes "written" without doing any writing.
 */
public class XZEncoder {

    private final NullOutputStream nos;
    private final LZMAOutputStream los;

    public XZEncoder(int level) throws Exception {
        LZMA2Options options = new LZMA2Options();
        nos = new NullOutputStream();

        if (level > 0 && level < 10) {
            options.setPreset(level);
        }

        los = new LZMAOutputStream(nos, options, false); // false prevents writing end marker
    }

    public XZEncoder() throws Exception {
        this(6);
    }

    public int encode(byte[] input) throws Exception {
        los.write(input, 0, input.length);
        return nos.size();
    }

    public void reset() {
        nos.resetByteCount();
    }
}
