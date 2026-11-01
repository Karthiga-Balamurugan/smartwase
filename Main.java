
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Main extends JFrame {

    private final CardLayout layout = new CardLayout();
    private final JPanel pages = new JPanel(layout);
    private final UserDAO userDAO = new UserDAO();
    private final CollectionReportDAO reportDAO = new CollectionReportDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final CollectionHistoryDAO historyDAO = new CollectionHistoryDAO();

    private String currentName = "";
    private String currentRole = "";
    private String currentUsername = "";

    private final Color blue = new Color(40, 90, 170);
    private final Color backgroundColor = new Color(240, 245, 252);

    private final RoadNetwork roadNetwork = createRoadNetwork();
    private final RoutePlanner routePlanner = new RoutePlanner(roadNetwork);

    public Main() {
        setTitle("SmartWaste System");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pages.add(loginPage(), "login");
        pages.add(registerPage(), "register");
        add(pages);
        layout.show(pages, "login");
    }

    private RoadNetwork createRoadNetwork() {
        RoadNetwork network = new RoadNetwork();

        // Demo road distances (km) for the 20 unique dustbin locations.
        network.addRoad("Central Market", "Bus Stand", 4);
        network.addRoad("Bus Stand", "College Road", 5);
        network.addRoad("College Road", "Government Hospital", 3);
        network.addRoad("Government Hospital", "Residential Area - North", 6);
        network.addRoad("Residential Area - North", "Public Park", 4);
        network.addRoad("Central Market", "College Road", 7);
        network.addRoad("Bus Stand", "Residential Area - North", 8);
        network.addRoad("College Road", "Public Park", 6);
        network.addRoad("Central Market", "Government Hospital", 10);

        network.addRoad("Central Market", "Railway Station", 3);
        network.addRoad("Bus Stand", "Municipal Office", 2);
        network.addRoad("College Road", "Main Shopping Street", 2);
        network.addRoad("Government Hospital", "Post Office", 4);
        network.addRoad("Bus Stand", "Police Station", 3);
        network.addRoad("College Road", "Government School", 4);
        network.addRoad("Public Park", "Water Tank Junction", 3);
        network.addRoad("Central Market", "Temple Street", 2);
        network.addRoad("Central Market", "Market Road", 1);
        network.addRoad("Residential Area - North", "Community Hall", 2);
        network.addRoad("Public Park", "Sports Ground", 2);
        network.addRoad("Government Hospital", "Industrial Estate", 8);
        network.addRoad("Public Park", "Lake View Road", 5);
        network.addRoad("Residential Area - North", "Residential Area - South", 4);

        return network;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 15));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField textField() {
        JTextField t = new JTextField();
        t.setMaximumSize(new Dimension(350, 36));
        t.setFont(new Font("Arial", Font.PLAIN, 15));
        return t;
    }

    private JPasswordField passwordField() {
        JPasswordField p = new JPasswordField();
        p.setMaximumSize(new Dimension(350, 36));
        p.setFont(new Font("Arial", Font.PLAIN, 15));
        return p;
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(350, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBackground(blue);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        p.setPreferredSize(new Dimension(440, 500));
        return p;
    }

    private void addField(JPanel panel, String title, JComponent field) {
        panel.add(label(title));
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));
    }

    private JPanel loginPage() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(backgroundColor);

        JPanel form = formPanel();

        JLabel title = new JLabel("SMARTWASTE");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(blue);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField username = textField();
        JPasswordField password = passwordField();

        JComboBox<String> role =
                new JComboBox<>(new String[]{"Admin", "Worker"});
        role.setMaximumSize(new Dimension(350, 36));

        JButton login = button("LOGIN");
        JButton register = button("REGISTER NEW ACCOUNT");

        form.add(title);
        form.add(Box.createVerticalStrut(20));
        addField(form, "Login As", role);
        addField(form, "Username", username);
        addField(form, "Password", password);
        form.add(login);
        form.add(Box.createVerticalStrut(10));
        form.add(register);
        form.add(Box.createVerticalStrut(15));
        form.add(label("<html>Demo Admin: admin / admin123<br>"
                + "Demo Worker: worker / worker123</html>"));

        login.addActionListener(e -> {
            String u = username.getText().trim();
            String p = new String(password.getPassword());
            String r = (String) role.getSelectedItem();

            if (u.isEmpty() || p.isEmpty()) {
                message("Enter username and password.");
                return;
            }

            UserDAO.UserRecord account = userDAO.loginUser(u, p, r);

            if (account != null) {
                currentName = account.name;
                currentRole = account.role;
                currentUsername = account.username;
                dashboard();
            } else {
                message("Invalid username, password or role.");
            }
        });

        register.addActionListener(e -> layout.show(pages, "register"));

        background.add(form);
        return background;
    }

    private JPanel registerPage() {
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(backgroundColor);

        JPanel form = formPanel();

        JLabel title = new JLabel("CREATE ACCOUNT");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(blue);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField name = textField();
        JTextField username = textField();
        JPasswordField password = passwordField();
        JPasswordField confirm = passwordField();

        JComboBox<String> role =
                new JComboBox<>(new String[]{"Worker", "Admin"});
        role.setMaximumSize(new Dimension(350, 36));

        JButton register = button("REGISTER");
        JButton back = button("BACK TO LOGIN");

        form.add(title);
        form.add(Box.createVerticalStrut(15));
        addField(form, "Full Name", name);
        addField(form, "Username", username);
        addField(form, "Password", password);
        addField(form, "Confirm Password", confirm);
        addField(form, "Register As", role);
        form.add(register);
        form.add(Box.createVerticalStrut(10));
        form.add(back);

        register.addActionListener(e -> {
            String n = name.getText().trim();
            String u = username.getText().trim();
            String p = new String(password.getPassword());
            String c = new String(confirm.getPassword());
            String r = (String) role.getSelectedItem();

            if (n.isEmpty() || u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                message("Please fill all fields.");
                return;
            }

            if (p.length() < 6) {
                message("Password must contain at least 6 characters.");
                return;
            }

            if (!p.equals(c)) {
                message("Passwords do not match.");
                return;
            }

            if (userDAO.registerUser(n, u, p, r)) {
                message("Account created successfully.");
                name.setText("");
                username.setText("");
                password.setText("");
                confirm.setText("");
                layout.show(pages, "login");
            } else {
                message("Username already exists or registration failed.");
            }
        });

        back.addActionListener(e -> layout.show(pages, "login"));

        background.add(form);
        return background;
    }

    private void dashboard() {
        JFrame frame = new JFrame("SmartWaste Dashboard");
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel heading = new JLabel(
                "  SMARTWASTE - " + currentRole.toUpperCase() + " DASHBOARD");
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(blue);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(new Color(30, 49, 78));
        menu.setPreferredSize(new Dimension(225, 0));

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] options;

        if ("Admin".equalsIgnoreCase(currentRole)) {
            options = new String[]{
                "Dashboard", "Waste Collection Analytics", "Pending Reports", "Assigned Collections", "Dustbin Monitor",
                "Collection Vehicles", "Assign Collection",
                "Smart Routes", "Collection History"
            };
        } else {
            options = new String[]{
                "Dashboard", "Submit Overflow Report",
                "My Collection Tasks", "Update Status",
                "Collection History"
            };
        }

        for (String option : options) {
            JButton b = new JButton(option);
            b.setMaximumSize(new Dimension(215, 43));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(43, 66, 100));
            b.setFocusPainted(false);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(e -> showSection(content, option));
            menu.add(b);
            menu.add(Box.createVerticalStrut(8));
        }

        JButton logout = new JButton("LOGOUT");
        logout.setMaximumSize(new Dimension(215, 40));
        logout.setBackground(new Color(180, 50, 50));
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> frame.dispose());

        menu.add(Box.createVerticalGlue());
        menu.add(logout);
        menu.add(Box.createVerticalStrut(15));

        frame.add(heading, BorderLayout.NORTH);
        frame.add(menu, BorderLayout.WEST);
        frame.add(content, BorderLayout.CENTER);

        showSection(content, "Dashboard");
        frame.setVisible(true);
    }

    private void showSection(JPanel content, String section) {
        content.removeAll();

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);

        JLabel title = new JLabel(section);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(blue);
        body.add(title);
        body.add(Box.createVerticalStrut(20));

        switch (section) {
            case "Dashboard":
                body.add(label("Welcome, " + currentName + "!"));
                body.add(label("Role: " + currentRole));
                body.add(label("Smart City Waste Collection System"));
                body.add(Box.createVerticalStrut(15));
                body.add(label("HIGH priority: 90% fill and above"));
                body.add(label("MEDIUM priority: 70% to 89%"));
                body.add(label("LOW priority: below 70%"));
                if ("Admin".equalsIgnoreCase(currentRole)) addAnalyticsCards(body);
                break;

            case "Waste Collection Analytics":
                showAnalytics(body);
                break;

            case "Assigned Collections":
                showAssignedCollections(body);
                break;

            case "Submit Overflow Report":
                reportForm(body);
                break;

            case "Pending Reports":
                showPendingReports(body);
                break;

            case "Dustbin Monitor":
                showDustbins(body);
                break;

            case "Collection Vehicles":
                showVehicles(body);
                break;

            case "Assign Collection":
                assignmentForm(body);
                break;

            case "Smart Routes":
                routeForm(body);
                break;

            case "My Collection Tasks":
                showWorkerTasks(body);
                break;

            case "Update Status":
                updateStatusForm(body);
                break;

            case "Collection History":
                showHistory(body);
                break;

            default:
                body.add(label("Section not available."));
        }

        content.add(new JScrollPane(body), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void reportForm(JPanel panel) {
        JTextField binId = textField();
        JSpinner fill = new JSpinner(new SpinnerNumberModel(90, 0, 100, 1));
        JLabel binDetails = label("Enter a Dustbin ID and click CHECK ID.");
        final CollectionReportDAO.DustbinRecord[] selectedBin = new CollectionReportDAO.DustbinRecord[1];

        addField(panel, "Dustbin ID (example: D001)", binId);
        addField(panel, "Fill Level (0-100%)", fill);
        JButton check = button("CHECK DUSTBIN ID");
        JButton submit = button("SUBMIT REPORT");
        panel.add(check);
        panel.add(Box.createVerticalStrut(8));
        panel.add(binDetails);
        panel.add(Box.createVerticalStrut(12));
        panel.add(submit);

        check.addActionListener(e -> {
            selectedBin[0] = reportDAO.findDustbin(binId.getText().trim());
            if (selectedBin[0] == null) {
                binDetails.setText("Dustbin ID Not Found. Please check the ID.");
            } else {
                binDetails.setText("Verified: " + selectedBin[0].binId + " | Location: "
                        + selectedBin[0].location + " | Current Fill: "
                        + selectedBin[0].fillLevel + "%");
            }
        });

        submit.addActionListener(e -> {
            CollectionReportDAO.DustbinRecord bin = reportDAO.findDustbin(binId.getText().trim());
            if (bin == null) {
                selectedBin[0] = null;
                binDetails.setText("Dustbin ID Not Found. Please check the ID.");
                message("Invalid Dustbin ID. Report was not submitted.");
                return;
            }
            boolean success = reportDAO.submitReport(bin.binId, (Integer) fill.getValue(), currentUsername);
            if (success) {
                message("Report submitted successfully for " + bin.binId + " - " + bin.location);
                binId.setText("");
                fill.setValue(90);
                selectedBin[0] = null;
                binDetails.setText("Report submitted. Enter another Dustbin ID.");
            } else {
                message("Report not submitted. This dustbin may already have an active report, or the database may be unavailable.");
            }
        });
    }

    private void showPendingReports(JPanel panel) {
        List<CollectionReportDAO.CollectionReport> reports = reportDAO.getPendingReports();
        if (reports.isEmpty()) {
            panel.add(label("No pending reports found."));
            return;
        }
        // PriorityQueue orders reports by combined priority and distance score.
        PriorityQueue<CollectionReportDAO.CollectionReport> queue = new PriorityQueue<>(
                (a, b) -> {
                    int combined = Integer.compare(collectionScore(b), collectionScore(a));
                    if (combined != 0) return combined;
                    return Integer.compare(distanceFromDepot(a.location), distanceFromDepot(b.location));
                });
        queue.addAll(reports);
        reports = new ArrayList<>();
        while (!queue.isEmpty()) reports.add(queue.poll());
        String[] columns = {"Report ID", "Bin ID", "Location", "Fill %", "Priority", "Priority Score", "Distance", "Collection Score", "Status", "Reported At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (CollectionReportDAO.CollectionReport r : reports) {
            model.addRow(new Object[]{r.reportId, r.binId, r.location, r.fillLevel, r.priority,
                    CollectionReportDAO.getPriorityScore(r.fillLevel), distanceFromDepot(r.location) + " km",
                    collectionScore(r), r.status, r.reportedAt});
        }
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table));
        panel.add(Box.createVerticalStrut(10));
        panel.add(label("Collection Score = Priority Score - (Distance x 2). Higher score is handled first; route distance is calculated from Central Market."));
        JTextField reportId = textField();
        addField(panel, "Selected / Pending Report ID", reportId);
        JButton assign = button("ASSIGN WORKER + VEHICLE AUTOMATICALLY");
        panel.add(assign);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                reportId.setText(String.valueOf(table.getValueAt(table.getSelectedRow(), 0)));
            }
        });
        assign.addActionListener(e -> assignReportAutomatically(reportId.getText().trim()));
    }

    private String normalizeLocation(String location) {
        if (location == null) return null;
        String key = location.trim().toLowerCase().replaceAll("[^a-z]", "");
        for (String known : roadNetwork.getAllLocations()) {
            if (known.toLowerCase().replaceAll("[^a-z]", "").equals(key)) return known;
        }
        if (key.equals("publicpark")) return "Public Park";
        if (key.equals("centralmarket")) return "Central Market";
        if (key.equals("busstand")) return "Bus Stand";
        if (key.equals("collegeroad")) return "College Road";
        if (key.equals("residentialarea")) return "Residential Area";
        return null;
    }

    private int distanceFromDepot(String location) {
        String normalized = normalizeLocation(location);
        if (normalized == null || !roadNetwork.containsLocation(normalized)) return Integer.MAX_VALUE / 4;
        return routePlanner.getRouteDistance("Central Market", normalized);
    }

    private int collectionScore(CollectionReportDAO.CollectionReport report) {
        int distance = distanceFromDepot(report.location);
        if (distance >= Integer.MAX_VALUE / 8) return CollectionReportDAO.getPriorityScore(report.fillLevel);
        return CollectionReportDAO.getPriorityScore(report.fillLevel) - (distance * 2);
    }

    private void assignReportAutomatically(String reportIdText) {
        try {
            int reportId = Integer.parseInt(reportIdText);
            String location = getReportLocation(reportId);
            if (location == null) {
                message("Report ID not found.");
                return;
            }
            String routeDestination = normalizeLocation(location);
            if (routeDestination == null || !roadNetwork.containsLocation(routeDestination)) {
                message("No road-network data for this dustbin location: " + location);
                return;
            }
            List<String> route = routePlanner.findShortestRoute("Central Market", routeDestination);
            int distance = routePlanner.getRouteDistance("Central Market", routeDestination);
            if (route.isEmpty() || distance < 0) {
                message("No route found for this dustbin.");
                return;
            }
            String routeText = String.join(" -> ", route);
            if (assignmentDAO.autoAssignCollection(reportId, routeText, distance)) {
                String details = getAssignmentDetails(reportId);
                message("WORK ASSIGNED SUCCESSFULLY\n\n" + details
                        + "\nRoute: " + routeText + "\nDistance: " + distance + " km");
            } else {
                message("Automatic assignment failed. Check that the report is pending and at least one worker and vehicle are available.");
            }
        } catch (NumberFormatException ex) {
            message("Enter or select a valid Report ID.");
        }
    }

    private String getReportLocation(int reportId) {
        String sql = "SELECT d.location FROM collection_reports r JOIN dustbins d ON d.bin_id = r.bin_id WHERE r.report_id = ?";
        try (Connection con = DBConnection.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString("location") : null; }
        } catch (SQLException e) {
            message("Could not read report location: " + e.getMessage());
            return null;
        }
    }

    private int queryCount(String sql) {
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.out.println("Analytics query failed: " + e.getMessage());
            return 0;
        }
    }

    private void addAnalyticsCards(JPanel panel) {
        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 10));
        cards.setBackground(Color.WHITE);
        cards.setMaximumSize(new Dimension(900, 100));
        cards.add(metricCard("TOTAL DUSTBINS", queryCount("SELECT COUNT(*) FROM dustbins"), new Color(42, 103, 170)));
        cards.add(metricCard("PENDING REPORTS", queryCount("SELECT COUNT(*) FROM collection_reports WHERE UPPER(report_status) IN ('PENDING','REPORTED')"), new Color(210, 137, 35)));
        cards.add(metricCard("ACTIVE ASSIGNMENTS", queryCount("SELECT COUNT(*) FROM assignments WHERE UPPER(status) IN ('ASSIGNED','IN PROGRESS','IN_PROGRESS')"), new Color(115, 87, 175)));
        cards.add(metricCard("COMPLETED COLLECTIONS", queryCount("SELECT COUNT(*) FROM collection_history"), new Color(40, 145, 100)));
        panel.add(cards);
        panel.add(Box.createVerticalStrut(12));
        panel.add(label("Overflow alerts (90% and above): " + queryCount("SELECT COUNT(*) FROM dustbins WHERE fill_level >= 90")));
        panel.add(label("Analytics are calculated from the current SQLite database records."));
    }

    private JPanel metricCard(String heading, int value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(new Color(245, 248, 253));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel h = new JLabel(heading);
        h.setFont(new Font("Arial", Font.BOLD, 11));
        h.setForeground(color);
        JLabel v = new JLabel(String.valueOf(value));
        v.setFont(new Font("Arial", Font.BOLD, 25));
        v.setForeground(new Color(35, 48, 65));
        card.add(h);
        card.add(v);
        return card;
    }

    private void showAnalytics(JPanel panel) {
        panel.add(label("Live summary of waste collection activity"));
        panel.add(Box.createVerticalStrut(12));
        addAnalyticsCards(panel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(label("Dustbin fill-level summary"));
        DefaultTableModel model = new DefaultTableModel(new String[]{"Fill Category", "Dustbin Count"}, 0);
        model.addRow(new Object[]{"Critical (90-100%)", queryCount("SELECT COUNT(*) FROM dustbins WHERE fill_level BETWEEN 90 AND 100")});
        model.addRow(new Object[]{"Needs Collection (70-89%)", queryCount("SELECT COUNT(*) FROM dustbins WHERE fill_level BETWEEN 70 AND 89")});
        model.addRow(new Object[]{"Normal (0-69%)", queryCount("SELECT COUNT(*) FROM dustbins WHERE fill_level BETWEEN 0 AND 69")});
        JTable table = new JTable(model);
        table.setRowHeight(28);
        panel.add(new JScrollPane(table));
        JButton refresh = button("REFRESH ANALYTICS");
        panel.add(Box.createVerticalStrut(12));
        panel.add(refresh);
        refresh.addActionListener(e -> { panel.removeAll(); showAnalytics(panel); panel.revalidate(); panel.repaint(); });
    }

    private String getAssignmentDetails(int reportId) {
        String sql = "SELECT a.assignment_id, a.worker_username, COALESCE(u.name, a.worker_username) AS worker_name, a.vehicle_id "
                + "FROM assignments a LEFT JOIN users u ON u.username = a.worker_username "
                + "WHERE a.report_id = ? ORDER BY a.assignment_id DESC LIMIT 1";
        try (Connection con = DBConnection.connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return "Assignment ID: " + rs.getInt("assignment_id")
                        + "\nWorker: " + rs.getString("worker_name") + " (" + rs.getString("worker_username") + ")"
                        + "\nVehicle: " + rs.getString("vehicle_id");
            }
        } catch (SQLException e) {
            return "Assignment saved, but worker details could not be loaded: " + e.getMessage();
        }
        return "Assignment saved. Worker details unavailable.";
    }

    private void showAssignedCollections(JPanel panel) {
        String sql = "SELECT a.assignment_id, r.report_id, r.bin_id, d.location, "
                + "COALESCE(u.name, a.worker_username) AS worker_name, a.worker_username, "
                + "a.vehicle_id, a.route, a.distance, a.status, a.assigned_at "
                + "FROM assignments a JOIN collection_reports r ON r.report_id = a.report_id "
                + "JOIN dustbins d ON d.bin_id = r.bin_id LEFT JOIN users u ON u.username = a.worker_username "
                + "ORDER BY a.assignment_id DESC";
        DefaultTableModel model = new DefaultTableModel(new String[]{"Assignment", "Report", "Dustbin", "Location", "Worker Name", "Username", "Vehicle", "Route", "Distance", "Status", "Assigned At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        try (Connection con = DBConnection.connect(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) model.addRow(new Object[]{rs.getInt("assignment_id"), rs.getInt("report_id"), rs.getString("bin_id"), rs.getString("location"), rs.getString("worker_name"), rs.getString("worker_username"), rs.getString("vehicle_id"), rs.getString("route"), rs.getDouble("distance"), rs.getString("status"), rs.getString("assigned_at")});
        } catch (SQLException e) { panel.add(label("Could not load assignments: " + e.getMessage())); }
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table));
        JButton refresh = button("REFRESH ASSIGNMENTS");
        panel.add(Box.createVerticalStrut(10));
        panel.add(refresh);
        refresh.addActionListener(e -> { panel.removeAll(); showAssignedCollections(panel); panel.revalidate(); panel.repaint(); });
    }

    private void showDustbins(JPanel panel) {
        String sql = "SELECT bin_id, location, fill_level, status "
                + "FROM dustbins ORDER BY fill_level DESC";

        String[] columns = {"Bin ID", "Location", "Fill %", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("bin_id"),
                    rs.getString("location"),
                    rs.getInt("fill_level"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            message("Could not load dustbins: " + e.getMessage());
        }

        panel.add(new JScrollPane(new JTable(model)));
    }

    private void showVehicles(JPanel panel) {
        JTextField id = textField();
        JTextField driver = textField();
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(150, 1, 10000, 10));

        addField(panel, "Vehicle ID", id);
        addField(panel, "Driver Name", driver);
        addField(panel, "Capacity (kg)", capacity);

        JButton add = button("ADD VEHICLE");
        panel.add(add);

        add.addActionListener(e -> {
            if (assignmentDAO.addVehicle(
                    id.getText().trim(),
                    driver.getText().trim(),
                    (Integer) capacity.getValue())) {
                message("Vehicle saved.");
                id.setText("");
                driver.setText("");
                showSection(panel.getParent() instanceof JPanel
                        ? (JPanel) panel.getParent() : panel, "Collection Vehicles");
            } else {
                message("Vehicle could not be saved. Check fields or existing ID.");
            }
        });

        panel.add(Box.createVerticalStrut(15));
        panel.add(label("Vehicles stored in database:"));

        String sql = "SELECT vehicle_id, driver_name, capacity, available "
                + "FROM vehicles ORDER BY vehicle_id";

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Vehicle ID", "Driver", "Capacity", "Available"}, 0);

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("vehicle_id"),
                    rs.getString("driver_name"),
                    rs.getInt("capacity"),
                    rs.getInt("available") == 1 ? "Yes" : "No"
                });
            }
        } catch (SQLException e) {
            message("Could not load vehicles: " + e.getMessage());
        }

        panel.add(new JScrollPane(new JTable(model)));
    }

    private void assignmentForm(JPanel panel) {
        JTextField reportId = textField();
        addField(panel, "Pending Report ID", reportId);
        panel.add(label("Admin reviews the report and clicks Assign. Worker, vehicle and route are selected automatically."));
        panel.add(Box.createVerticalStrut(12));
        JButton assign = button("ASSIGN AUTOMATICALLY");
        panel.add(assign);
        assign.addActionListener(e -> assignReportAutomatically(reportId.getText().trim()));
    }

    private void loadAvailableVehicles(JComboBox<String> combo) {
        combo.removeAllItems();

        String sql = "SELECT vehicle_id FROM vehicles "
                + "WHERE available = 1 ORDER BY vehicle_id";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                combo.addItem(rs.getString("vehicle_id"));
            }
        } catch (SQLException e) {
            message("Could not load available vehicles: " + e.getMessage());
        }
    }

    private void routeForm(JPanel panel) {
        JComboBox<String> source = new JComboBox<>(
                roadNetwork.getAllLocations().toArray(new String[0]));
        JComboBox<String> destination = new JComboBox<>(
                roadNetwork.getAllLocations().toArray(new String[0]));

        addField(panel, "Start Location", source);
        addField(panel, "Destination", destination);

        JTextArea result = new JTextArea(5, 30);
        result.setEditable(false);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);

        JButton calculate = button("FIND SHORTEST COLLECTION ROUTE");
        panel.add(calculate);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JScrollPane(result));

        calculate.addActionListener(e -> {
            String s = (String) source.getSelectedItem();
            String d = (String) destination.getSelectedItem();

            if (s == null || d == null) {
                result.setText("No locations available.");
                return;
            }

            List<String> route = routePlanner.findShortestRoute(s, d);
            int distance = routePlanner.getRouteDistance(s, d);

            if (route.isEmpty() || distance < 0) {
                result.setText("No route found.");
            } else {
                result.setText("Shortest route:\n"
                        + String.join(" -> ", route)
                        + "\nTotal distance: " + distance
                        + " distance units");
            }
        });
    }

    private void showWorkerTasks(JPanel panel) {
        String sql = "SELECT a.assignment_id, a.report_id, r.bin_id, "
                + "d.location, r.fill_level, a.vehicle_id, a.route, a.distance, a.status "
                + "FROM assignments a "
                + "JOIN collection_reports r ON a.report_id = r.report_id "
                + "JOIN dustbins d ON r.bin_id = d.bin_id "
                + "WHERE a.worker_username = ? "
                + "ORDER BY r.fill_level DESC";

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Assignment", "Report", "Bin", "Location",
                    "Fill %", "Vehicle", "Route", "Distance", "Status"}, 0);

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, currentUsername);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("assignment_id"),
                        rs.getInt("report_id"),
                        rs.getString("bin_id"),
                        rs.getString("location"),
                        rs.getInt("fill_level"),
                        rs.getString("vehicle_id"),
                        rs.getString("route"),
                        rs.getInt("distance"),
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            message("Could not load tasks: " + e.getMessage());
        }

        panel.add(new JScrollPane(new JTable(model)));
    }

    private void updateStatusForm(JPanel panel) {
        JTextField assignmentId = textField();
        addField(panel, "Assignment ID", assignmentId);
        panel.add(label("Start work first, then click Mark as Collected after the dustbin is cleared."));
        panel.add(Box.createVerticalStrut(10));
        JButton start = button("MARK AS IN PROGRESS");
        JButton complete = button("MARK AS COLLECTED");
        panel.add(start);
        panel.add(Box.createVerticalStrut(8));
        panel.add(complete);

        start.addActionListener(e -> updateWorkerAssignment(assignmentId.getText().trim(), "In Progress"));
        complete.addActionListener(e -> updateWorkerAssignment(assignmentId.getText().trim(), "COMPLETED"));
    }

    private void updateWorkerAssignment(String idText, String status) {
        try {
            int id = Integer.parseInt(idText);
            if (!isAssignmentOwnedByCurrentWorker(id)) {
                message("This assignment does not belong to your account.");
                return;
            }
            if (assignmentDAO.updateAssignmentStatus(id, status)) {
                message("Collection status updated to " + status + ".");
            } else {
                message("Status update failed. Check assignment ID and current status.");
            }
        } catch (NumberFormatException ex) {
            message("Enter a valid Assignment ID.");
        }
    }

    private boolean isAssignmentOwnedByCurrentWorker(int assignmentId) {
        String sql = "SELECT worker_username FROM assignments WHERE assignment_id = ?";

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assignmentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        && currentUsername.equals(rs.getString("worker_username"));
            }
        } catch (SQLException e) {
            message("Could not verify assignment: " + e.getMessage());
            return false;
        }
    }

    private void showHistory(JPanel panel) {
        List<CollectionHistoryDAO.HistoryRecord> history =
                historyDAO.getCollectionHistory();

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"History ID", "Assignment", "Bin ID", "Location",
                    "Worker", "Vehicle", "Route", "Distance", "Status", "Completed At"}, 0);

        for (CollectionHistoryDAO.HistoryRecord h : history) {
            if ("Admin".equalsIgnoreCase(currentRole)
                    || currentUsername.equals(h.workerUsername)) {
                model.addRow(new Object[]{
                    h.historyId, h.assignmentId, h.binId, h.location,
                    h.workerUsername, h.vehicleId, h.route, h.distance,
                    h.status, h.updatedAt
                });
            }
        }

        if (model.getRowCount() == 0) {
            panel.add(label("No completed collection history found."));
        } else {
            panel.add(new JScrollPane(new JTable(model)));
        }
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}