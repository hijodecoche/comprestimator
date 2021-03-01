package edu.umn.power327.database;

import edu.umn.power327.CompressionResult;

import java.sql.*;

/**
 * Interacts with local SQLite DB VERSION 100
 * Will hold file sha256 hash, file extension, compress time in microseconds,
 * orig and compressed file sizes in bytes, and results of `file` command.
 */
public class DBController {

    private final Connection con;

    // Table names
    public static String DEFLATE1 = "deflate1_results";
    public static String DEFLATE6 = "deflate6_results";
    public static String DEFLATE9 = "deflate9_results";
    public static String LZ4 = "lz4_results";
    public static String LZ4HC = "lz4hc_results";
    public static String XZ6 = "xz6_results";
    public static String XZ9 = "xz9_results";

    // TODO: Always change version id when altering this file
    public static int VERSION = 101;

    public DBController() throws SQLException {
        con = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    /**
     * Creates tables for storing results.
     */
    public void createTables() {
        String defaultSchema = "hash CHAR(64) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size INT NOT NULL,\n"
                + "compress_size INT NOT NULL,\n"
                + "compress_time INT NOT NULL,\n"
                + "file_type VARCHAR(32),\n"
                + "PRIMARY KEY(hash, orig_size));";
        try {
            Statement stmt = con.createStatement();

            // set version id
            stmt.execute("PRAGMA application_id=" + VERSION + ";");

            stmt.execute("CREATE TABLE IF NOT EXISTS " + DEFLATE1 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + DEFLATE6 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + DEFLATE9 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + LZ4 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + LZ4HC + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + XZ6 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + XZ9 + "(\n" + defaultSchema);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param table name of compressor used + "_results"
     * @param hash 16-byte SHA256 hash of file
     * @param file_ext file's extension
     * @param origSize original size in kb
     * @param compressSize compressed size in kb
     * @param compressTime time it took to compress file, in milliseconds
     * @throws SQLException if table does not exist
     */
    public void insertResult(String table, String hash, String file_ext, int origSize,
                             int compressSize, long compressTime, String type) throws SQLException {
        Statement s = con.createStatement();
        s.execute("INSERT OR IGNORE INTO " + table + " (hash, file_ext, orig_size, "
                + "compress_size, compress_time, file_type) VALUES('" + hash + "', '" + file_ext + "', "
                + origSize + ", " + compressSize + ", " + compressTime + ", '" + type + "');");
    }

    /**
     * Convenience method for abstracting the compression result.
     * @param table table to store the result.
     * @param result CompressionResult object with necessary values.
     * @throws SQLException if table does not exist
     */
    public void insertResult(String table, CompressionResult result) throws SQLException {
        insertResult(table, result.getHash(), result.getExt(), result.getOrigSize(),
                result.getCompressSize(), result.getCompressTime(), result.getType());
    }

    /**
     * Checks if a file has already been compressed using specified compression algorithm.
     * @param table name of table to check
     * @param hash hash value of file
     * @param origSize original size of file
     * @return true if database contains results from this file, else false
     * @throws SQLException if given table name that does not exist
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
     * @return true if database contains results from this file in all tables, else false
     */
    public boolean contains(String hash, long origSize) {
        String[] tables = {DEFLATE1, DEFLATE6, DEFLATE9, LZ4, LZ4HC, XZ6, XZ9};
        boolean inAllTables = true;
        for (String table : tables) {
            try {
                if (!contains(table, hash, origSize)) {
                    inAllTables = false;
                }
            } catch (SQLException ignored) { } // some tables may not exist. continue.
        }

        return inAllTables;
    }

    /**
     * Deletes an entry from a single table.
     * Uses hash, origSize to identify the unique file using primary key.
     * @param table table containing entry to delete
     * @param hash hash of file
     * @param origSize original size of file
     * @throws SQLException if table does not exist
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
        String[] tables = {DEFLATE1, DEFLATE6, DEFLATE9, LZ4, LZ4HC, XZ6, XZ9};

        for(String table : tables) {
            try {
                deleteResult(table, hash, origSize);
            } catch (SQLException ignored){ }
        }
    }
}
