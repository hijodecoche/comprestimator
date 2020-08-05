package SevenZip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Pared implementation of LZMA specifically for use in comprestimator.
 * To mimic java.util.zip.Deflater, most of instantiation occurs in ctor
 * and reset() method to discard old dictionary, etc.
 * TODO: maybe make outStream a public field, so rather than encode method
 *      returning int, the user calls outStream.size() outside timer?
 */
public class LzmaEncoder {

    private SevenZip.Compression.LZMA.Encoder encoder;

    public LzmaEncoder() throws Exception {
        encoder = new SevenZip.Compression.LZMA.Encoder();
        if (!encoder.SetAlgorithm(2))
            throw new Exception("Incorrect compression mode");
        if (!encoder.SetDictionarySize(1 << 23))
            throw new Exception("Incorrect dictionary size");
        if (!encoder.SetNumFastBytes(128))
            throw new Exception("Incorrect -fb value");
        if (!encoder.SetMatchFinder(1))
            throw new Exception("Incorrect -mf value");
        if (!encoder.SetLcLpPb(3, 0, 2))
            throw new Exception("Incorrect -lc or -lp or -pb value");
        encoder.SetEndMarkerMode(false);
    }

    public int encode(byte[] input) throws Exception {
        long fileSize = input.length;
        ByteArrayInputStream inStream = new ByteArrayInputStream(input);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        encoder.WriteCoderProperties(outStream);
        for (int i = 0; i < 8; i++)
            outStream.write((int)(fileSize >>> (8 * i)) & 0xFF);
        encoder.Code(inStream, outStream, -1, -1, null);

        return outStream.size(); // signifies testing
    }

    public void reset() {
        encoder = new SevenZip.Compression.LZMA.Encoder();
        encoder.SetAlgorithm(2);
        encoder.SetDictionarySize(1 << 23);
        encoder.SetNumFastBytes(128);
        encoder.SetMatchFinder(1);
        encoder.SetLcLpPb(3, 0, 2);
        encoder.SetEndMarkerMode(false);
    }
}
