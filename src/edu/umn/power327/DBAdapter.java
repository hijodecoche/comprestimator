package edu.umn.power327;

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
    public String url = "jdbc:sqlite:c:\\sqlite\\java\\results.db";
    private final PreparedStatement createTable;
    private final PreparedStatement insert;

    /**
     * Create a table for storing results.
     * TODO: Needs something to check for proper execution of stmt.
     * @param name name of the table
     * @throws SQLException
     */
    public void createTable(String name) throws SQLException {
        createTable.setString(1, name);
        createTable.execute();
    }

    /**
     * TODO: Make this an interface or java version of a struct!!!
     * @param algorithm
     * @param hash
     * @param file_ext
     * @param origSize
     * @param compressSize
     * @param compressTime
     * @throws SQLException
     */
    public void insertResult(String algorithm, String hash, String file_ext, int origSize,
                             int compressSize, int compressTime) throws SQLException {
        insert.setString(1, algorithm + "_results");
        insert.setString(2, hash);
        insert.setString(3, file_ext);
        insert.setInt(4, origSize);
        insert.setInt(5, compressSize);
        insert.setInt(6, compressTime);

        insert.execute();
    }

    public DBAdapter() throws SQLException {
        try {
            con = DriverManager.getConnection(url);
            createTable = con.prepareStatement("CREATE TABLE IF NOT EXISTS ? (\n"
                    + "fileID INT PRIMARY KEY AUTO_INCREMENT,\n"
                    + "hash CHAR(16) NOT NULL,\n"
                    + "file_ext VARCHAR(8) NOT NULL,\n"
                    + "orig_size INT NOT NULL,\n"
                    + "compress_size INT NOT NULL,\n"
                    + "compress_time INT NOT NULL,\n"
                    + ");");
            insert = con.prepareStatement("INSERT INTO ?(hash, file_ext, orig_size, "
                    + "compress_size, compress_time) VALUES(?, ?, ?, ?, ?);");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
