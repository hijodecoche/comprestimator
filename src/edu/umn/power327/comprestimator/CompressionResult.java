package edu.umn.power327.comprestimator;

public class CompressionResult {


    private String hash;
    private String ext;
    private String type = "";
    /* Storing compressTime as long allows for the long times that lzma uses to be represented
     * without some kind of flag value.  SQLite can handle up to 64-bit integers to be store in INT
     */
    private long compressTime;
    private int origSize;
    private int compressSize;
    private int bytecount;
    private int bytecount2;
    private double entropy;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) throws Exception {
        // hash stored as hex chars
        if (hash.length() > 64) {
            System.out.println("hash length: " + hash.length());
            throw new Exception("Hash digest too long.");
        }
        this.hash = hash;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public long getCompressTime() {
        return compressTime;
    }

    public void setCompressTime(long compressTime) {
        this.compressTime = compressTime;
    }

    public int getOrigSize() {
        return origSize;
    }

    public void setOrigSize(int origSize) {
        this.origSize = origSize;
    }

    public int getCompressSize() {
        return compressSize;
    }

    public void setCompressSize(int compressSize) {
        this.compressSize = compressSize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBytecount() {
        return bytecount;
    }

    public void setBytecount(int bytecount) {
        this.bytecount = bytecount;
    }

    public int getBytecount2() {
        return bytecount2;
    }

    public void setBytecount2(int bytecount2) {
        this.bytecount2 = bytecount2;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
    }
    /**
     * Used for debugging or when bypassing database.
     */
    public void printToConsole() {
        System.out.println("Hash: " + this.hash);
        System.out.println("orig_size: " + this.origSize);
        System.out.println("compress_size: " + this.compressSize);
        System.out.println("compress_time: " + this.compressTime);
        System.out.println("Extension: " + this.ext);
        System.out.println("Type: " + this.type);
        System.out.println("Bytecount: " + this.bytecount);
        System.out.println("Bytecount2: " + this.bytecount2);
        System.out.println("Shannon Entropy: " + this.entropy);
    }
}
