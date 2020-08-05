package edu.umn.power327.database;

import java.sql.*;

/**
 * Interacts with local SQLite DB.
 * TODO: Is 8 chars enough for file extension?
 * TODO: Ctor should detect filesystem to determine db location.
 * Will hold file sha256 hash, unique fileID, compress time in millis,
 * orig and compressed file sizes in bytes.
 */
public class DBAdapter {
    private final Connection con;
    public String url = "jdbc:sqlite:C:/sqlite/test.db";

    public DBAdapter() throws SQLException {
        con = DriverManager.getConnection(url);
    }

    /**
     * Create a table for storing results.
     * TODO: Needs something to check for proper execution of stmt.
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        String deflateTable = "CREATE TABLE IF NOT EXISTS deflate_results(\n"
                + "fileID INTEGER PRIMARY KEY ASC,\n"
                + "hash CHAR(32) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size DOUBLE NOT NULL,\n"
                + "compress_size DOUBLE NOT NULL,\n"
                + "compress_time INTEGER NOT NULL\n"
                + ");";
        String LZ4Table = "CREATE TABLE IF NOT EXISTS lz4_results(\n"
                + "fileID INTEGER PRIMARY KEY ASC,\n"
                + "hash CHAR(32) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size INT NOT NULL,\n"
                + "compress_size INT NOT NULL,\n"
                + "compress_time INT NOT NULL\n"
                + ");";
        String LZMATable = "CREATE TABLE IF NOT EXISTS lzma_results(\n"
                + "fileID INTEGER PRIMARY KEY ASC,\n"
                + "hash CHAR(32) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size INT NOT NULL,\n"
                + "compress_size INT NOT NULL,\n"
                + "compress_time INT NOT NULL\n"
                + ");";
        Statement stmt = con.createStatement();
//        System.out.println(deflateTable);
        stmt.execute(deflateTable);
        stmt.execute(LZ4Table);
        stmt.execute(LZMATable);
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
    public void insertResult(String table, String hash, String file_ext, double origSize,
                             double compressSize, int compressTime) throws SQLException {
        Statement s = con.createStatement();

        s.execute("INSERT INTO " + table + " (hash, file_ext, orig_size, "
                + "compress_size, compress_time) VALUES('" + hash + "', '" + file_ext + "', "
                + origSize + ", " + compressSize + ", " + compressTime + ");");
    }
}
