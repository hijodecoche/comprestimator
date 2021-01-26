package edu.umn.power327.database;

import edu.umn.power327.CompressionResult;

import java.sql.*;

/**
 * Interacts with local SQLite DB.
 * Will hold file sha256 hash, unique fileID, compress time in millis,
 * orig and compressed file sizes in bytes.
 */
public class DBController {
    private final Connection con;

    public DBController() throws SQLException {
        con = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    /**
     * Creates tables for storing results.
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        String defaultSchema = "hash CHAR(64) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size INT NOT NULL,\n"
                + "compress_size INT NOT NULL,\n"
                + "compress_time INT NOT NULL,\n"
                + "PRIMARY KEY(hash, orig_size));";
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS deflate1_results(\n" + defaultSchema);
        stmt.execute("CREATE TABLE IF NOT EXISTS deflate6_results(\n" + defaultSchema);
        stmt.execute("CREATE TABLE IF NOT EXISTS deflate9_results(\n" + defaultSchema);
        stmt.execute("CREATE TABLE IF NOT EXISTS lz4_results(\n" + defaultSchema);
        stmt.execute("CREATE TABLE IF NOT EXISTS lz4hc_results(\n" + defaultSchema);
        stmt.execute("CREATE TABLE IF NOT EXISTS lzma_results(\n" + defaultSchema);
    }

    /**
     * @param table name of compressor used + "_results"
     * @param hash 16-byte SHA256 hash of file
     * @param file_ext file's extension
     * @param origSize original size in kb
     * @param compressSize compressed size in kb
     * @param compressTime time it took to compress file, in milliseconds
     * @throws SQLException
     */
    public void insertResult(String table, String hash, String file_ext, int origSize,
                             int compressSize, long compressTime) throws SQLException {
        Statement s = con.createStatement();

        s.execute("INSERT OR IGNORE INTO " + table + " (hash, file_ext, orig_size, "
                + "compress_size, compress_time) VALUES('" + hash + "', '" + file_ext + "', "
                + origSize + ", " + compressSize + ", " + compressTime + ");");
    }

    /**
     * Convenience method for abstracting the compression result.
     * @param table table to store the result.
     * @param result CompressionResult object with necessary values.
     * @throws SQLException
     */
    public void insertResult(String table, CompressionResult result) throws SQLException {
        insertResult(table, result.getHash(), result.getExt(), result.getOrigSize(),
                result.getCompressSize(), result.getCompressTime());
    }

    /**
     * Checks if a file has already been compressed.
     * @param table name of table to check
     * @param hash hash value of file
     * @param origSize original size of file
     * @return true if database contains results from this file, else false
     * @throws SQLException
     */
    public boolean contains(String table, String hash, long origSize) throws SQLException {

        Statement s = con.createStatement();
        s.execute("SELECT hash, orig_size FROM " + table + " WHERE hash='" + hash
                + "' AND orig_size=" + origSize);
        return s.getResultSet().next();
    }

    /**
     * Convenience method.  We'll generally use this in case we start changing table names, in which case
     * we don't need to go searching for calls to above function.
     * @param hash hash value of file
     * @param origSize original size of file
     * @return true if database contains results from this file, else false
     * @throws SQLException
     */
    public boolean contains(String hash, long origSize) throws SQLException {
        return contains("lzma_results", hash, origSize);
    }

    /**
     * Deletes an entry from a single table.  Currently used for running test on single file, if file to test has
     * already been stored. Uses hash, origSize to identify the unique file using primary key.
     * @param table table containing entry to delete
     * @param hash hash of file
     * @param origSize original size of file
     * @throws SQLException
     */
    public void deleteResult(String table, String hash, long origSize) throws SQLException {
        Statement s = con.createStatement();
        s.execute("DELETE FROM " + table + " WHERE hash='" + hash
                + "' AND orig_size=" + origSize);
    }

    /**
     * Deletes an entry from all tables using primary key (hash, origSize).
     * @param hash hash value of file
     * @param origSize original size of file
     */
    public void deleteFromAll(String hash, long origSize) {
        String[] tables = {"deflate1_results", "deflate6_results", "deflate9_results",
                "lz4_results", "lz4hc_results", "lzma_results"};

        for(String table : tables) {
            try {
                deleteResult(table, hash, origSize);
            } catch (SQLException ignored){ }
        }
    }
}
