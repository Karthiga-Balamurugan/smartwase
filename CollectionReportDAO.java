import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CollectionReportDAO {

    public CollectionReportDAO() {
        createTables();
        seedDustbins();
    }

    private void createTables() {
        String dustbinSQL = "CREATE TABLE IF NOT EXISTS dustbins ("
                + "bin_id TEXT PRIMARY KEY, location TEXT NOT NULL,"
                + "fill_level INTEGER NOT NULL DEFAULT 0,"
                + "status TEXT NOT NULL DEFAULT 'Normal',"
                + "updated_at TEXT DEFAULT CURRENT_TIMESTAMP)";
        String reportSQL = "CREATE TABLE IF NOT EXISTS collection_reports ("
                + "report_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "bin_id TEXT NOT NULL, reported_by TEXT NOT NULL,"
                + "fill_level INTEGER NOT NULL, priority TEXT NOT NULL,"
                + "report_status TEXT NOT NULL DEFAULT 'Pending',"
                + "reported_at TEXT DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(bin_id) REFERENCES dustbins(bin_id))";
        try (Connection con = DBConnection.connect(); Statement st = con.createStatement()) {
            st.executeUpdate(dustbinSQL);
            st.executeUpdate(reportSQL);
        } catch (SQLException e) {
            System.out.println("Report table setup failed: " + e.getMessage());
        }
    }

    private void seedDustbins() {
        // Every dustbin has one unique physical location.
        // Existing bin IDs are retained; only their demo locations are corrected.
        String sql = "INSERT INTO dustbins (bin_id, location, fill_level, status) "
                + "VALUES (?, ?, 0, 'Normal') "
                + "ON CONFLICT(bin_id) DO UPDATE SET location = excluded.location";
        String[][] bins = {
            {"D001", "Central Market"},
            {"D002", "Bus Stand"},
            {"D003", "College Road"},
            {"D004", "Government Hospital"},
            {"D005", "Residential Area - North"},
            {"D006", "Public Park"},
            {"D007", "Railway Station"},
            {"D008", "Municipal Office"},
            {"D009", "Main Shopping Street"},
            {"D010", "Post Office"},
            {"D011", "Police Station"},
            {"D012", "Government School"},
            {"D013", "Water Tank Junction"},
            {"D014", "Temple Street"},
            {"D015", "Market Road"},
            {"D016", "Community Hall"},
            {"D017", "Sports Ground"},
            {"D018", "Industrial Estate"},
            {"D019", "Lake View Road"},
            {"D020", "Residential Area - South"}
        };
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             Statement st = con.createStatement()) {
            for (String[] bin : bins) {
                ps.setString(1, bin[0]);
                ps.setString(2, bin[1]);
                ps.executeUpdate();
            }
            // Prevent duplicate locations even if letter case or extra spaces differ.
            st.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS idx_dustbins_unique_location "
                    + "ON dustbins(LOWER(TRIM(location)))");
        } catch (SQLException e) {
            System.out.println("Default dustbin setup failed: " + e.getMessage());
        }
    }

    public DustbinRecord findDustbin(String binId) {
        if (binId == null || binId.trim().isEmpty()) return null;
        String sql = "SELECT bin_id, location, fill_level, status FROM dustbins WHERE UPPER(bin_id) = UPPER(?)";
        try (Connection con = DBConnection.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, binId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new DustbinRecord(rs.getString("bin_id"), rs.getString("location"), rs.getInt("fill_level"), rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Dustbin lookup failed: " + e.getMessage());
        }
        return null;
    }

    // The worker enters only Dustbin ID and Fill Level. Location is retrieved from the master record.
    public boolean submitReport(String binId, int fillLevel, String reportedBy) {
        if (binId == null || binId.trim().isEmpty() || reportedBy == null || reportedBy.trim().isEmpty() || fillLevel < 0 || fillLevel > 100) return false;
        DustbinRecord bin = findDustbin(binId);
        if (bin == null) return false;

        String duplicateSQL = "SELECT 1 FROM collection_reports WHERE bin_id = ? AND UPPER(report_status) IN ('PENDING','REPORTED','ASSIGNED','IN PROGRESS','IN_PROGRESS') LIMIT 1";
        String updateBinSQL = "UPDATE dustbins SET fill_level = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE bin_id = ?";
        String reportSQL = "INSERT INTO collection_reports (bin_id, reported_by, fill_level, priority, report_status) VALUES (?, ?, ?, ?, 'Pending')";
        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(duplicateSQL)) {
                    ps.setString(1, bin.binId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { con.rollback(); return false; }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(updateBinSQL)) {
                    ps.setInt(1, fillLevel);
                    ps.setString(2, fillLevel >= 90 ? "Overflow Alert" : fillLevel >= 70 ? "Needs Collection" : "Reported");
                    ps.setString(3, bin.binId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(reportSQL)) {
                    ps.setString(1, bin.binId);
                    ps.setString(2, reportedBy.trim());
                    ps.setInt(3, fillLevel);
                    ps.setString(4, getPriority(fillLevel));
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Report submission failed: " + e.getMessage());
                return false;
            } finally { con.setAutoCommit(true); }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Backward-compatible overload for older screens/code.
    public boolean submitReport(String binId, String ignoredLocation, int fillLevel, String reportedBy) {
        return submitReport(binId, fillLevel, reportedBy);
    }

    public static int getPriorityScore(int fillLevel) {
        if (fillLevel >= 90) return 300 + fillLevel;
        if (fillLevel >= 70) return 200 + fillLevel;
        return 100 + fillLevel;
    }

    private String getPriority(int fillLevel) {
        if (fillLevel >= 90) return "HIGH";
        if (fillLevel >= 70) return "MEDIUM";
        return "LOW";
    }

    public List<CollectionReport> getPendingReports() {
        List<CollectionReport> reports = new ArrayList<>();
        String sql = "SELECT r.report_id, r.bin_id, r.reported_by, r.fill_level, r.priority, r.report_status, r.reported_at, d.location "
                + "FROM collection_reports r JOIN dustbins d ON d.bin_id = r.bin_id "
                + "WHERE UPPER(r.report_status) IN ('PENDING','REPORTED') "
                + "ORDER BY CASE UPPER(r.priority) WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 ELSE 1 END DESC, r.fill_level DESC, r.reported_at ASC";
        try (Connection con = DBConnection.connect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) reports.add(new CollectionReport(rs.getInt("report_id"), rs.getString("bin_id"), rs.getString("reported_by"), rs.getInt("fill_level"), rs.getString("priority"), rs.getString("report_status"), rs.getString("reported_at"), rs.getString("location")));
        } catch (SQLException e) { System.out.println("Unable to load reports: " + e.getMessage()); }
        return reports;
    }

    public boolean updateReportStatus(int reportId, String status) {
        String sql = "UPDATE collection_reports SET report_status = ? WHERE report_id = ?";
        try (Connection con = DBConnection.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, reportId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("Status update failed: " + e.getMessage()); return false; }
    }

    public static class DustbinRecord {
        public final String binId, location, status;
        public final int fillLevel;
        public DustbinRecord(String binId, String location, int fillLevel, String status) { this.binId=binId; this.location=location; this.fillLevel=fillLevel; this.status=status; }
    }

    public static class CollectionReport {
        public final int reportId, fillLevel;
        public final String binId, reportedBy, priority, status, reportedAt, location;
        public CollectionReport(int reportId, String binId, String reportedBy, int fillLevel, String priority, String status, String reportedAt) {
            this(reportId, binId, reportedBy, fillLevel, priority, status, reportedAt, "");
        }
        public CollectionReport(int reportId, String binId, String reportedBy, int fillLevel, String priority, String status, String reportedAt, String location) {
            this.reportId=reportId; this.binId=binId; this.reportedBy=reportedBy; this.fillLevel=fillLevel; this.priority=priority; this.status=status; this.reportedAt=reportedAt; this.location=location;
        }
    }
}
