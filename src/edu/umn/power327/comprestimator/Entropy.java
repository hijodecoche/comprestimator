package edu.umn.power327.comprestimator;

import java.util.Arrays;

public class Entropy {
    static int[] freqCount = new int[256]; // since our max file size is less than Integer.MAX_VALUE, int is safe
    public static double entropy;
    public static int bytecount; // for bytes where count > n (OG bytecount)
    public static int bytecount2; // for bytes where count >=n

    public static void calcEntropyAndBC(byte[] bytes) {
        Arrays.fill(freqCount, 0); // reset count
        int n = bytes.length / 256; // threshold for bytecount

        for (byte b : bytes) {
            freqCount[(int) b & 0xFF]++;
        }

        double prob;
        entropy = 0;
        bytecount = 0;
        bytecount2 = 0;

        for (int i : freqCount) {
            if (i >= n)
                bytecount2++;
            if (i > n)
                bytecount++;
            if (i != 0) {
                prob = ((double) i) / bytes.length; // P(x_i)
                entropy -= prob * (Math.log10(prob) / Math.log10(2.0)); // P(x_i) * log2(P(x_i))
            }
        }
    }
}
