package JDBCTask;
import java.sql.*;
import java.util.Scanner;

public class ComplaintManagementSystem {
    static final String URL = "jdbc:mysql://localhost:3306/complaint_db";
    static final String USER = "root";
    static final String PASSWORD = "Dhaya@21052004";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Database Connected Successfully!");
            createTables(con);

            insertOfficers(con);
            int choice;

            do {

                System.out.println("\n===== COMPLAINT MANAGEMENT SYSTEM =====");
                System.out.println("1. Register Complaint");
                System.out.println("2. View Complaints");
                System.out.println("3. Assign Officer");
                System.out.println("4. Update Status");
                System.out.println("5. View Resolution");
                System.out.println("6. Delete Complaint");
                System.out.println("7. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1:

                        System.out.print("Enter citizen name: ");
                        String name = sc.nextLine();
                        System.out.print("Enter phone number: ");
                        String phone = sc.nextLine();
                        System.out.print("Enter complaint: ");
                        String complaint = sc.nextLine();

                        registerComplaint(con, name, phone, complaint);
                        break;

                    case 2:
                        viewComplaints(con);
                        break;

                    case 3:

                        System.out.print("Enter complaint ID: ");
                        int complaintId = sc.nextInt();
                        System.out.print("Enter officer ID: ");
                        int officerId = sc.nextInt();

                        assignOfficer(con, complaintId, officerId);
                        break;

                    case 4:

                        System.out.print("Enter complaint ID: ");
                        int updateId = sc.nextInt();
                        sc.nextLine();

                        System.out.println("\n1. REGISTERED");
                        System.out.println("2. IN_PROGRESS");
                        System.out.println("3. RESOLVED");
                        System.out.println("4. REJECTED");
                        System.out.print("Select status: ");

                        int statusChoice = sc.nextInt();
                        sc.nextLine();
                        String status;

                        switch (statusChoice) {

                            case 1:
                                status = "REGISTERED";
                                break;

                            case 2:
                                status = "IN_PROGRESS";
                                break;

                            case 3:
                                status = "RESOLVED";
                                break;

                            case 4:
                                status = "REJECTED";
                                break;

                            default:
                                System.out.println("Invalid status!");
                                continue;
                        }

                        System.out.print("Enter resolution: ");
                        String resolution = sc.nextLine();
                        updateStatus(con, updateId, status, resolution);
                        break;

                    case 5:
                        System.out.print("Enter complaint ID: ");
                        int resolutionId = sc.nextInt();
                        viewResolution(con, resolutionId);
                        break;

                    case 6:
                        System.out.print("Enter complaint ID to delete: ");
                        int deleteId = sc.nextInt();
                        deleteComplaint(con, deleteId);
                        break;

                    case 7:
                        System.out.println("Thank you for using Complaint Management System!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }
            } while (choice != 7);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    // CREATE TABLES
    static void createTables(Connection con) throws SQLException {

        Statement stmt = con.createStatement();

        String usersTable = "CREATE TABLE IF NOT EXISTS users (user_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), phone VARCHAR(20))";
        String officersTable = "CREATE TABLE IF NOT EXISTS officers (officer_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), department VARCHAR(100))";
        String complaintsTable = "CREATE TABLE IF NOT EXISTS complaints (complaint_id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, officer_id INT, complaint_text VARCHAR(500), status VARCHAR(30), resolution VARCHAR(500), FOREIGN KEY(user_id) REFERENCES users(user_id), FOREIGN KEY(officer_id) REFERENCES officers(officer_id))";

        stmt.executeUpdate(usersTable);
        stmt.executeUpdate(officersTable);
        stmt.executeUpdate(complaintsTable);
        System.out.println("Tables Created Successfully!");
    }

    // INSERT OFFICERS
    static void insertOfficers(Connection con) throws SQLException {

        String sql = "INSERT IGNORE INTO officers (officer_id, name, department) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, 1);
        ps.setString(2, "Arun Kumar");
        ps.setString(3, "Water Department");
        ps.executeUpdate();

        ps.setInt(1, 2);
        ps.setString(2, "Priya");
        ps.setString(3, "Road Department");
        ps.executeUpdate();

        ps.setInt(1, 3);
        ps.setString(2, "Bala");
        ps.setString(3, "Electricity Department");
        ps.executeUpdate();

        ps.close();
    }

    // REGISTER COMPLAINT
    static void registerComplaint(Connection con, String name, String phone, String complaint) throws SQLException {

        String userSql = "INSERT INTO users (name, phone) VALUES (?, ?)";
        PreparedStatement userPs = con.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
        userPs.setString(1, name);
        userPs.setString(2, phone);
        userPs.executeUpdate();
        ResultSet keys = userPs.getGeneratedKeys();
        keys.next();
        int userId = keys.getInt(1);

        String complaintSql = "INSERT INTO complaints (user_id, complaint_text, status) VALUES (?, ?, ?)";
        PreparedStatement complaintPs = con.prepareStatement(complaintSql);
        complaintPs.setInt(1, userId);
        complaintPs.setString(2, complaint);
        complaintPs.setString(3, "REGISTERED");
        complaintPs.executeUpdate();
        System.out.println("\nComplaint Registered Successfully!");
        keys.close();
        userPs.close();
        complaintPs.close();
    }

    static void viewComplaints(Connection con) throws SQLException {

        String sql = "SELECT c.complaint_id, u.name, u.phone, c.complaint_text, o.name AS officer, c.status, c.resolution FROM complaints c JOIN users u ON c.user_id = u.user_id LEFT JOIN officers o ON c.officer_id = o.officer_id";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n===== ALL COMPLAINTS =====");

        while (rs.next()) {

            System.out.println("\nComplaint ID : " + rs.getInt("complaint_id"));
            System.out.println("Citizen      : " + rs.getString("name"));
            System.out.println("Phone        : " + rs.getString("phone"));
            System.out.println("Complaint    : " + rs.getString("complaint_text"));
            System.out.println("Officer      : " + rs.getString("officer"));
            System.out.println("Status       : " + rs.getString("status"));
            System.out.println("Resolution   : " + rs.getString("resolution"));
        }
        rs.close();
        ps.close();
    }

    static void assignOfficer(Connection con, int complaintId, int officerId) throws SQLException {

        String sql = "UPDATE complaints SET officer_id = ?, status = 'IN_PROGRESS' WHERE complaint_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, officerId);
        ps.setInt(2, complaintId);
        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Officer Assigned Successfully!");
            System.out.println("Complaint Status: IN_PROGRESS");
        } else {
            System.out.println("Complaint not found!");
        }
        ps.close();
    }

    // UPDATE STATUS
    static void updateStatus(Connection con, int complaintId, String status, String resolution) throws SQLException {

        String sql = "UPDATE complaints SET status = ?, resolution = ? WHERE complaint_id = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, status);
        ps.setString(2, resolution);
        ps.setInt(3, complaintId);
        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Complaint Status Updated Successfully!");
            System.out.println("New Status: " + status);

        } else {
            System.out.println("Complaint not found!");
        }
        ps.close();
    }

    // VIEW RESOLUTION
    static void viewResolution(Connection con, int complaintId) throws SQLException {

        String sql = "SELECT c.complaint_id, u.name, c.complaint_text, c.status, c.resolution, o.name AS officer FROM complaints c JOIN users u ON c.user_id = u.user_id LEFT JOIN officers o ON c.officer_id = o.officer_id WHERE c.complaint_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, complaintId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("\n===== COMPLAINT RESOLUTION =====");
            System.out.println("Complaint ID : " + rs.getInt("complaint_id"));
            System.out.println("Citizen      : " + rs.getString("name"));
            System.out.println("Complaint    : " + rs.getString("complaint_text"));
            System.out.println("Officer      : " + rs.getString("officer"));
            System.out.println("Status       : " + rs.getString("status"));
            System.out.println("Resolution   : " + rs.getString("resolution"));
        }
        else {
            System.out.println("Complaint not found!");
        }
        rs.close();
        ps.close();
    }

    // DELETE COMPLAINT
    static void deleteComplaint(Connection con, int complaintId) throws SQLException {

        String sql = "DELETE FROM complaints WHERE complaint_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, complaintId);
        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Complaint Deleted Successfully!");

        } else {
            System.out.println("Complaint not found!");
        }
        ps.close();
    }
}

