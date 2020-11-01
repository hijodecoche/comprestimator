package edu.umn.power327.database;

import edu.umn.power327.CompressionResult;

import java.sql.*;

/**
 * Interacts with local SQLite DB.
 * TODO: Is 8 chars enough for file extension?
 * Will hold file sha256 hash, unique fileID, compress time in millis,
 * orig and compressed file sizes in bytes.
 */
public class DBController {
    private final Connection con;

    public DBController() throws SQLException {
        con = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    /**
     * Create a table for storing results.
     * TODO: Needs something to check for proper execution of stmt.
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        String defaultSchema = "hash CHAR(64) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size INT NOT NULL,\n"
                + "compress_size INT NOT NULL,\n"
                + "compress_time INT NOT NULL\n"
                + ");";
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

        s.execute("INSERT INTO " + table + " (hash, file_ext, orig_size, "
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
}
