package edu.umn.power327;

public class CompressionResult {

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) throws Exception {
        // hash stored as hex chars
        if (hash.length() != 64) {
            System.out.println("hash length: " + hash.length());
            throw new Exception("Incorrect hash length.");
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
        if (compressSize > 0)
            this.compressSize = compressSize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
    }

    private String hash;
    private String ext;
    private String type = "";
    /* Storing compressTime as long allows for the long times that lzma uses to be represented
     * without some kind of flag value.  SQLite can handle up to 64-bit integers to be store in INT
     */
    private long compressTime;
    private int origSize;
    private int compressSize;
}
