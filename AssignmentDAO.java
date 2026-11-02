
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AssignmentDAO {

    public AssignmentDAO() {
        createTables();
        seedDemoVehicles();
    }

    private void createTables() {
        String vehicleSQL =
                "CREATE TABLE IF NOT EXISTS vehicles ("
                + "vehicle_id TEXT PRIMARY KEY,"
                + "driver_name TEXT NOT NULL,"
                + "capacity INTEGER NOT NULL,"
                + "available INTEGER NOT NULL DEFAULT 1)";

        String assignmentSQL =
                "CREATE TABLE IF NOT EXISTS assignments ("
                + "assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "report_id INTEGER NOT NULL,"
                + "worker_username TEXT NOT NULL,"
                + "vehicle_id TEXT NOT NULL,"
                + "route TEXT,"
                + "distance INTEGER DEFAULT 0,"
                + "status TEXT NOT NULL DEFAULT 'Pending',"
                + "assigned_at TEXT DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(report_id) REFERENCES collection_reports(report_id),"
                + "FOREIGN KEY(vehicle_id) REFERENCES vehicles(vehicle_id))";

        String historySQL =
                "CREATE TABLE IF NOT EXISTS collection_history ("
                + "history_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "assignment_id INTEGER NOT NULL,"
                + "bin_id TEXT NOT NULL,"
                + "worker_username TEXT NOT NULL,"
                + "final_status TEXT NOT NULL,"
                + "completed_at TEXT DEFAULT CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.connect();
             Statement stmt = con.createStatement()) {

            stmt.executeUpdate(vehicleSQL);
            stmt.executeUpdate(assignmentSQL);
            stmt.executeUpdate(historySQL);

        } catch (SQLException e) {
            System.out.println(
                    "Assignment table setup failed: " + e.getMessage());
        }
    }

    private void seedDemoVehicles() {
        addVehicle("V001", "Arun", 150);
        addVehicle("V002", "Kumar", 200);
        addVehicle("V003", "Ravi", 100);
    }

    // Admin clicks Assign; worker and available vehicle are selected automatically.
    public boolean autoAssignCollection(int reportId, String route, int distance) {
        if (reportId <= 0 || route == null || route.trim().isEmpty() || distance < 0) return false;
        String workerSQL = "SELECT u.username FROM users u WHERE UPPER(u.role) = 'WORKER' "
                + "AND NOT EXISTS (SELECT 1 FROM assignments a WHERE a.worker_username = u.username "
                + "AND UPPER(a.status) IN ('ASSIGNED','IN PROGRESS','IN_PROGRESS')) "
                + "ORDER BY u.username LIMIT 1";
        String vehicleSQL = "SELECT vehicle_id FROM vehicles WHERE available = 1 ORDER BY capacity DESC, vehicle_id LIMIT 1";
        String worker = null;
        String vehicle = null;
        try (Connection con = DBConnection.connect();
             PreparedStatement workerPS = con.prepareStatement(workerSQL);
             ResultSet workerRS = workerPS.executeQuery()) {
            if (workerRS.next()) worker = workerRS.getString("username");
        } catch (SQLException e) {
            System.out.println("Worker availability check failed: " + e.getMessage());
            return false;
        }
        try (Connection con = DBConnection.connect();
             PreparedStatement vehiclePS = con.prepareStatement(vehicleSQL);
             ResultSet vehicleRS = vehiclePS.executeQuery()) {
            if (vehicleRS.next()) vehicle = vehicleRS.getString("vehicle_id");
        } catch (SQLException e) {
            System.out.println("Vehicle availability check failed: " + e.getMessage());
            return false;
        }
        if (worker == null || vehicle == null) return false;
        return assignCollection(reportId, worker, vehicle, route, distance);
    }

    public boolean addVehicle(
            String vehicleId, String driverName, int capacity) {

        if (vehicleId == null || vehicleId.trim().isEmpty()
                || driverName == null || driverName.trim().isEmpty()
                || capacity <= 0) {
            return false;
        }

        String sql = "INSERT OR IGNORE INTO vehicles "
                + "(vehicle_id, driver_name, capacity, available) "
                + "VALUES (?, ?, ?, 1)";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, vehicleId.trim());
            ps.setString(2, driverName.trim());
            ps.setInt(3, capacity);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Vehicle save failed: " + e.getMessage());
            return false;
        }
    }

    public boolean assignCollection(
            int reportId,
            String workerUsername,
            String vehicleId,
            String route,
            int distance) {

        if (reportId <= 0 || workerUsername == null
                || workerUsername.trim().isEmpty()
                || vehicleId == null || vehicleId.trim().isEmpty()
                || route == null || route.trim().isEmpty()
                || distance < 0) {
            return false;
        }

        String checkSQL =
                "SELECT report_status FROM collection_reports "
                + "WHERE report_id = ?";

        String vehicleSQL =
                "SELECT available FROM vehicles WHERE vehicle_id = ?";

        String insertSQL =
                "INSERT INTO assignments "
                + "(report_id, worker_username, vehicle_id, route, distance, status) "
                + "VALUES (?, ?, ?, ?, ?, 'Assigned')";

        String updateReportSQL =
                "UPDATE collection_reports "
                + "SET report_status = 'Assigned' WHERE report_id = ?";

        String updateVehicleSQL =
                "UPDATE vehicles SET available = 0 WHERE vehicle_id = ?";

        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);

            try {
                String reportStatus;

                try (PreparedStatement ps = con.prepareStatement(checkSQL)) {
                    ps.setInt(1, reportId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        reportStatus = rs.getString("report_status");
                    }
                }

                if (!"Pending".equalsIgnoreCase(reportStatus) && !"Reported".equalsIgnoreCase(reportStatus)) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement ps = con.prepareStatement(vehicleSQL)) {
                    ps.setString(1, vehicleId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt("available") != 1) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertSQL)) {
                    ps.setInt(1, reportId);
                    ps.setString(2, workerUsername.trim());
                    ps.setString(3, vehicleId.trim());
                    ps.setString(4, route);
                    ps.setInt(5, distance);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(updateReportSQL)) {
                    ps.setInt(1, reportId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(updateVehicleSQL)) {
                    ps.setString(1, vehicleId);
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Assignment failed: " + e.getMessage());
                return false;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAssignmentStatus(
            int assignmentId, String status) {

        if (!"In Progress".equals(status)
                && !"COMPLETED".equalsIgnoreCase(status)
                && !"Completed".equals(status)) {
            return false;
        }

        String selectSQL =
                "SELECT report_id, worker_username, vehicle_id "
                + ", status FROM assignments WHERE assignment_id = ?";

        String assignmentSQL =
                "UPDATE assignments SET status = ? "
                + "WHERE assignment_id = ?";

        String reportSQL =
                "UPDATE collection_reports SET report_status = ? "
                + "WHERE report_id = ?";

        String historySQL =
                "INSERT INTO collection_history "
                + "(assignment_id, bin_id, worker_username, final_status) "
                + "SELECT ?, bin_id, ?, ? FROM collection_reports "
                + "WHERE report_id = ?";

        String vehicleSQL =
                "UPDATE vehicles SET available = 1 WHERE vehicle_id = ?";
        String dustbinSQL = "UPDATE dustbins SET fill_level = 0, status = 'Collected', updated_at = CURRENT_TIMESTAMP "
                + "WHERE bin_id = (SELECT bin_id FROM collection_reports WHERE report_id = ?)";

        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);

            try {
                int reportId;
                String worker;
                String vehicle;
                String currentStatus;

                try (PreparedStatement ps = con.prepareStatement(selectSQL)) {
                    ps.setInt(1, assignmentId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        reportId = rs.getInt("report_id");
                        worker = rs.getString("worker_username");
                        vehicle = rs.getString("vehicle_id");
                        currentStatus = rs.getString("status");
                    }
                }

                if ("In Progress".equals(status) && !"Assigned".equalsIgnoreCase(currentStatus)) {
                    con.rollback();
                    return false;
                }
                if ("COMPLETED".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                    if (!"In Progress".equalsIgnoreCase(currentStatus)) {
                        con.rollback();
                        return false;
                    }
                    status = "COMPLETED";
                }

                try (PreparedStatement ps = con.prepareStatement(assignmentSQL)) {
                    ps.setString(1, status);
                    ps.setInt(2, assignmentId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(reportSQL)) {
                    ps.setString(1, status);
                    ps.setInt(2, reportId);
                    ps.executeUpdate();
                }

                if ("Completed".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                    status = "COMPLETED";
                    try (PreparedStatement ps = con.prepareStatement(historySQL)) {
                        ps.setInt(1, assignmentId);
                        ps.setString(2, worker);
                        ps.setString(3, status);
                        ps.setInt(4, reportId);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = con.prepareStatement(vehicleSQL)) {
                        ps.setString(1, vehicle);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(dustbinSQL)) {
                        ps.setInt(1, reportId);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Status update failed: " + e.getMessage());
                return false;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
}