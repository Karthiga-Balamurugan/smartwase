
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class UserDAO {

    public UserDAO() {
        createTable();
        addDefaultUsers();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL)";

        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("Users table is ready.");

        } catch (SQLException e) {
            System.out.println("Table creation failed:");
            e.printStackTrace();
        }
    }

    private void addDefaultUsers() {
        String sql = "INSERT OR IGNORE INTO users "
                + "(name, username, password, role) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "Administrator");
            ps.setString(2, "admin");
            ps.setString(3, "admin123");
            ps.setString(4, "Admin");
            ps.executeUpdate();

            ps.setString(1, "Collection Worker");
            ps.setString(2, "worker");
            ps.setString(3, "worker123");
            ps.setString(4, "Worker");
            ps.executeUpdate();

            System.out.println("Default users checked.");

        } catch (SQLException e) {
            System.out.println("Default user creation failed:");
            e.printStackTrace();
        }
    }

    public boolean registerUser(
            String name,
            String username,
            String password,
            String role) {

        String sql = "INSERT INTO users "
                + "(name, username, password, role) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, username.trim());
            ps.setString(3, password);
            ps.setString(4, role);

            int rowsInserted = ps.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println(
                        "User registered: " + username);

                return true;
            }

            return false;

        } catch (SQLException e) {
            System.out.println("Registration failed:");
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Registration failed:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }
    }

    public UserRecord loginUser(
            String username,
            String password,
            String role) {

        String sql = "SELECT name, username, role "
                + "FROM users "
                + "WHERE username = ? AND password = ? AND role = ?";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, role);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new UserRecord(
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("role")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Login failed:");
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Login database error:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        return null;
    }

    public static class UserRecord {
        public final String name;
        public final String username;
        public final String role;

        public UserRecord(
                String name,
                String username,
                String role) {

            this.name = name;
            this.username = username;
            this.role = role;
        }
    }
}
