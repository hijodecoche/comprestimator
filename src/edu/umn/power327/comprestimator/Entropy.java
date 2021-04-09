package edu.umn.power327.comprestimator;

import java.util.Arrays;

public class Entropy {

    public static double entropy;
    public static int bytecount; // for bytes where count > n (OG bytecount)
    public static int bytecount2; // for bytes where count >=n

    public static void calcEntropyAndBC(byte[] input) {
        bytecount = 0;
        bytecount2 = 0;
        int chunkCounter = 0;
        int[] totalFreq = new int[256]; // used to calc entropy
        int[] bcFreq = new int[256]; // used to calc BC for a 1k chunk of data
        Arrays.fill(totalFreq, 0); // reset count
        int slotnum;

        for (byte aByte : input) {
            if (chunkCounter >= 1024) {
                chunkCounter = 0;
                calcBC(bcFreq);
                Arrays.fill(bcFreq, 0);
            }

            slotnum = (int) aByte & 0xFF;
            totalFreq[slotnum]++;
            bcFreq[slotnum]++;
            chunkCounter++;
        }
        if (chunkCounter != 0)
            calcBC(bcFreq); // process partial chunk, if exists
        bytecount = (int) Math.ceil(bytecount / Math.ceil(input.length / 1024.0)); // avg BC per chunk
        bytecount2 = (int) Math.ceil(bytecount2 / Math.ceil(input.length / 1024.0)); // avg BC2 per chunk

        double probability;
        entropy = 0;

        for (int i : totalFreq) {
            if (i != 0) {
                probability = ((double) i) / input.length; // P(x_i)
                entropy -= probability * (Math.log10(probability) / Math.log10(2.0)); // P(x_i) * log2(P(x_i))
            }
        }
    }

    private static void calcBC(int[] bcFreq) {
        // threshold is always 4 since we are processing 1k chunks
        for (int i : bcFreq) {
            if (i >= 4)
                bytecount2++;
            if (i > 4)
                bytecount++;
        }
    }
}
