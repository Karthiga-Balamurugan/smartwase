
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static final File DB_FILE = new File(
            System.getProperty("user.dir"),
            "smartwaste.db"
    );

    private static final String URL =
            "jdbc:sqlite:" +
            DB_FILE.getAbsolutePath().replace('\\', '/');

    static {
        System.out.println("DATABASE FILE: "
                + DB_FILE.getAbsolutePath());
    }

    public static String getDatabasePath() {
        return DB_FILE.getAbsolutePath();
    }

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "SQLite JDBC driver not found. Add sqlite-jdbc JAR to Referenced Libraries.",
                    e
            );
        }

        return DriverManager.getConnection(URL);
    }

    public static void createTables() {
        String[] sqlStatements = {
            "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL)",

            "CREATE TABLE IF NOT EXISTS dustbins ("
                + "bin_id TEXT PRIMARY KEY, "
                + "location TEXT, "
                + "fill_level INTEGER, "
                + "status TEXT, "
                + "updated_at TEXT)",

            "CREATE TABLE IF NOT EXISTS vehicles ("
                + "vehicle_id TEXT PRIMARY KEY, "
                + "driver_name TEXT, "
                + "capacity INTEGER, "
                + "available INTEGER)",

            "CREATE TABLE IF NOT EXISTS collection_reports ("
                + "report_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "bin_id TEXT, "
                + "reported_by TEXT, "
                + "fill_level INTEGER, "
                + "priority TEXT, "
                + "report_status TEXT, "
                + "reported_at TEXT)",

            "CREATE TABLE IF NOT EXISTS assignments ("
                + "assignment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "report_id INTEGER, "
                + "worker_username TEXT, "
                + "vehicle_id TEXT, "
                + "route TEXT, "
                + "distance REAL, "
                + "status TEXT, "
                + "assigned_at TEXT)",

            "CREATE TABLE IF NOT EXISTS collection_history ("
                + "history_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "assignment_id INTEGER, "
                + "bin_id TEXT, "
                + "worker_username TEXT, "
                + "final_status TEXT, "
                + "completed_at TEXT)"
        };

        try (Connection con = connect();
             Statement stmt = con.createStatement()) {

            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
            }

            System.out.println("Database connected successfully.");
            System.out.println("Database path: " + getDatabasePath());

        } catch (SQLException e) {
            System.out.println("Database setup failed: "
                    + e.getMessage());
        }
    }

    public static void main(String[] args) {
        createTables();
    }
}
