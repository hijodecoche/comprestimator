package edu.umn.power327;

public class CompressionResult {

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) throws Exception {
        if (hash.length() != 32) {
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

    private String hash;
    private String ext;
    /* Storing compressTime as long allows for the long times that lzma uses to be represented
     * without some kind of flag value.  SQLite can handle up to 64-bit integers to be store in INT
     */
    private long compressTime;
    private int origSize;
    private int compressSize;
}
