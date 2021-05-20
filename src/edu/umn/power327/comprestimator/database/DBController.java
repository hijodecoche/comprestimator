package edu.umn.power327.comprestimator.database;

import edu.umn.power327.comprestimator.CompressionResult;

import java.sql.*;

/**
 * Interacts with local SQLite DB VERSION 103
 * Will hold file sha256 hash, file extension, compress time in microseconds,
 * orig and compressed file sizes in bytes, results of `file` command.
 */
public class DBController {

    // Table names
    public static final String DEFLATE1 = "deflate1_results";
    public static final String DEFLATE6 = "deflate6_results";
    public static final String DEFLATE9 = "deflate9_results";
    public static final String LZ4 = "lz4_results";
    public static final String LZ4HC = "lz4hc_results";
    public static final String XZ6 = "xz6_results";
    public static final String XZ9 = "xz9_results";

    private static DBController dbInstance;
    private final Connection con;
    private PreparedStatement xz9_contains;
    private PreparedStatement deflate1_insert;
    private PreparedStatement deflate6_insert;
    private PreparedStatement deflate9_insert;
    private PreparedStatement lz4_insert;
    private PreparedStatement lz4hc_insert;
    private PreparedStatement xz6_insert;
    private PreparedStatement xz9_insert;
    private PreparedStatement time_insert;
    private PreparedStatement time_delete;
    private int filesProcessed;

    // TODO: Always change version id when altering this file
    // The hundreds determines compatibility, e.g. 210 incompatible with 199, 100 compatible with 199
    public static int VERSION = 222;

    private DBController() throws SQLException {
        con = DriverManager.getConnection("jdbc:sqlite:test.db");
        createTables();
        prepareStatements();
        try (ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM XZ9_RESULTS;")) {
            filesProcessed = rs.getInt(1);
        } catch (SQLException e) {
            filesProcessed = 0;
        }
    }

    static {
        try {
            dbInstance = new DBController();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static DBController getInstance() {
        return dbInstance;
    }

    private void prepareStatements() throws SQLException {
        deflate1_insert = con.prepareStatement("INSERT OR IGNORE INTO " + DEFLATE1 +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        deflate6_insert = con.prepareStatement("INSERT OR IGNORE INTO " + DEFLATE6 +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        deflate9_insert = con.prepareStatement("INSERT OR IGNORE INTO " + DEFLATE9 +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        lz4_insert = con.prepareStatement("INSERT OR IGNORE INTO " + LZ4 +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        lz4hc_insert = con.prepareStatement("INSERT OR IGNORE INTO " + LZ4HC +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        xz6_insert = con.prepareStatement("INSERT OR IGNORE INTO " + XZ6 +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        xz9_insert = con.prepareStatement("INSERT OR IGNORE INTO " + XZ9 +
                "(hash, file_ext, orig_size, compress_size, compress_time, file_type, bytecount, bytecount2, entropy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");

        xz9_contains = con.prepareStatement("SELECT hash, orig_size FROM " + XZ9
                + " WHERE hash=? AND orig_size=?;");
        time_insert = con.prepareStatement("INSERT OR IGNORE INTO elapsed_time VALUES(?);");
        time_delete = con.prepareStatement("DELETE FROM elapsed_time WHERE e_time < ?;");
    }

    /**
     * Creates tables for storing results.
     */
    public void createTables() throws SQLException {
        String defaultSchema = "hash CHAR(64) NOT NULL,\n"
                + "file_ext VARCHAR(8) NOT NULL,\n"
                + "orig_size INT NOT NULL,\n"
                + "compress_size INT NOT NULL,\n"
                + "compress_time INT NOT NULL,\n"
                + "file_type VARCHAR(32),\n"
                + "bytecount INT NOT NULL,\n"
                + "bytecount2 INT NOT NULL,\n"
                + "entropy DOUBLE NOT NULL,\n"
                + "PRIMARY KEY(hash, orig_size));";

        try (Statement stmt = con.createStatement()) {
            if (getDBVersion() == 0) {
                stmt.execute("PRAGMA application_id=" + VERSION + ";");
            }
            // check if DB has incompatible version
            else if ((getDBVersion() / 100) != (VERSION / 100))
                throw new SQLException("Incompatible database version.");
            // set version id


            stmt.execute("CREATE TABLE IF NOT EXISTS " + DEFLATE1 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + DEFLATE6 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + DEFLATE9 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + LZ4 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + LZ4HC + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + XZ6 + "(\n" + defaultSchema);
            stmt.execute("CREATE TABLE IF NOT EXISTS " + XZ9 + "(\n" + defaultSchema);

            // create elapsed time table
            stmt.execute("CREATE TABLE IF NOT EXISTS elapsed_time (e_time INTEGER NOT NULL);");

        }
    }

    /**
     * General insert method to be used by individualized insert methods.
     * @param ps the PreparedStatement for the desired table.
     * @param result CompressionResult object with necessary values.
     * @throws SQLException if table does not exist
     */
    public void insertResult(PreparedStatement ps, CompressionResult result) throws SQLException {
        ps.setString(1, result.getHash());
        ps.setString(2, result.getExt());
        ps.setInt(3, result.getOrigSize());
        ps.setInt(4, result.getCompressSize());
        ps.setLong(5, result.getCompressTime());
        ps.setString(6, result.getType());
        ps.setInt(7, result.getBytecount());
        ps.setInt(8, result.getBytecount2());
        ps.setDouble(9, result.getEntropy());
        ps.executeUpdate();
        filesProcessed++;
    }

    public void insertDeflate1 (CompressionResult result) throws SQLException {
        insertResult(deflate1_insert, result);
    }
    public void insertDeflate6 (CompressionResult result) throws SQLException {
        insertResult(deflate6_insert, result);
    }
    public void insertDeflate9 (CompressionResult result) throws SQLException {
        insertResult(deflate9_insert, result);
    }
    public void insertLZ4 (CompressionResult result) throws SQLException {
        insertResult(lz4_insert, result);
    }
    public void insertLZ4HC (CompressionResult result) throws SQLException {
        insertResult(lz4hc_insert, result);
    }
    public void insertXZ6 (CompressionResult result) throws SQLException {
        insertResult(xz6_insert, result);
    }
    public void insertXZ9(CompressionResult result) throws SQLException {
        insertResult(xz9_insert, result);
    }

    /**
     * If exists in xz9_results, entry is guaranteed to exist in all other tables.
     * If it does not exist in xz9_results, it may still exist in previous tables,
     * but attempting to re-insert is safe.
     * @param hash hash value of file
     * @param origSize original size of file
     * @return true if database contains results from this file in all tables, else false
     */
    public boolean contains(String hash, long origSize) {
        boolean exists = false;
        try {
            xz9_contains.setString(1, hash);
            xz9_contains.setLong(2, origSize);
            try (ResultSet rs = xz9_contains.executeQuery()) {
                exists = rs.next();
            }
        } catch (SQLException ignored) { }
        return exists;
    }

    /**
     * Deletes an entry from a single table.
     * Uses hash, origSize to identify the unique file using primary key.
     * @param table table containing entry to delete
     * @param hash hash of file
     * @param origSize original size of file
     */
    public void deleteResult(String table, String hash, long origSize) {
        try (Statement s = con.createStatement()) {
            s.execute("DELETE FROM " + table + " WHERE hash='" + hash
                    + "' AND orig_size=" + origSize);
        } catch (SQLException ignored) {}
    }

    public void updateStartIndex(int index) throws SQLException {
        try (Statement s = con.createStatement()) {
            s.execute("PRAGMA user_version = " + index + ";");
        }
    }

    public int getStartIndex() {
        int startIndex = 0;
        try (Statement s = con.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT * from pragma_user_version;");
            startIndex = rs.getInt(1);
        } catch (SQLException ignored) {}
        return startIndex;
    }

    private int getDBVersion() {
        try (Statement s = con.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT * from pragma_application_id;");
            return rs.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return Integer.MAX_VALUE; // convinces DB that this version is incompatible
    }

    /**
     * Encapsulating this logic in DBController breaks good coding practices, but it allows
     * for much more accurate elapsed timing.
     *
     * @param time elapsed time in seconds
     */
    public void updateTime(int time) {
        try {
            time_insert.setInt(1, time);
            time_insert.executeUpdate();

            time_delete.setInt(1, time);
            time_delete.executeUpdate();
        } catch (SQLException ignored) { }
    }

    public int getElapsedTime() {
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT MAX(e_time) FROM elapsed_time;")) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            return 0;
        }
    }
    public int getFilesProcessed() {
        return filesProcessed;
    }
}
