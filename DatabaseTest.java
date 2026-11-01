

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseTest {
    public static void main(String[] args) {

        try (Connection con = DBConnection.connect();
             Statement st = con.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS users ("
                       + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                       + "name TEXT NOT NULL, "
                       + "username TEXT NOT NULL UNIQUE, "
                       + "password TEXT NOT NULL, "
                       + "role TEXT NOT NULL)";

            st.executeUpdate(sql);
            System.out.println("Users table created successfully!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}