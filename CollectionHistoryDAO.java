
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CollectionHistoryDAO {

    public List<HistoryRecord> getCollectionHistory() {
        List<HistoryRecord> list = new ArrayList<>();

        String sql =
                "SELECT h.history_id, h.assignment_id, h.bin_id, " +
                "h.worker_username, h.final_status, h.completed_at, " +
                "a.vehicle_id, a.route, a.distance, d.location " +
                "FROM collection_history h " +
                "LEFT JOIN assignments a " +
                "ON h.assignment_id = a.assignment_id " +
                "LEFT JOIN dustbins d ON h.bin_id = d.bin_id " +
                "ORDER BY h.history_id DESC";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new HistoryRecord(
                        rs.getInt("history_id"),
                        rs.getInt("assignment_id"),
                        rs.getString("bin_id"),
                        rs.getString("location"),
                        rs.getString("worker_username"),
                        rs.getString("vehicle_id"),
                        rs.getString("route"),
                        rs.getDouble("distance"),
                        rs.getString("final_status"),
                        rs.getString("completed_at")
                ));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error loading history: " + e.getMessage());
        }

        return list;
    }

    public static class HistoryRecord {
        public final int historyId;
        public final int assignmentId;
        public final String binId;
        public final String location;
        public final String workerUsername;
        public final String vehicleId;
        public final String route;
        public final double distance;
        public final String status;
        public final String updatedAt;

        public HistoryRecord(
                int historyId, int assignmentId, String binId,
                String location, String workerUsername,
                String vehicleId, String route, double distance,
                String status, String updatedAt) {
            this.historyId = historyId;
            this.assignmentId = assignmentId;
            this.binId = binId;
            this.location = location;
            this.workerUsername = workerUsername;
            this.vehicleId = vehicleId;
            this.route = route;
            this.distance = distance;
            this.status = status;
            this.updatedAt = updatedAt;
        }
    }
}