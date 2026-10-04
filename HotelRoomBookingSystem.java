 package JDBCTask;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class HotelRoomBookingSystem {

    static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    static final String USER = "root";
    static final String PASSWORD = "Dhaya@21052004";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Database Connected Successfully!");
            createTables(con);
            insertRooms(con);
            int choice;

            do {

                System.out.println("\n===== HOTEL ROOM BOOKING SYSTEM =====");
                System.out.println("1. Room Availability");
                System.out.println("2. Booking");
                System.out.println("3. Check-In");
                System.out.println("4. Check-Out");
                System.out.println("5. Billing");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        showAvailableRooms(con);
                        break;

                    case 2:
                        System.out.print("Enter customer name: ");
                        String name = sc.nextLine();
                        System.out.print("Enter phone number: ");
                        String phone = sc.nextLine();
                        System.out.print("Enter room number: ");
                        int roomNumber = sc.nextInt();
                        System.out.print("Enter number of days: ");
                        int days = sc.nextInt();
                        bookRoom(con, name, phone, roomNumber, days);
                        break;

                    case 3:
                        System.out.print("Enter room number for Check-In: ");
                        int checkInRoom = sc.nextInt();
                        checkIn(con, checkInRoom);
                        break;

                    case 4:
                        System.out.print("Enter room number for Check-Out: ");
                        int checkOutRoom = sc.nextInt();
                        checkOut(con, checkOutRoom);
                        break;

                    case 5:
                        System.out.print("Enter room number for Billing: ");
                        int billingRoom = sc.nextInt();
                        showBill(con, billingRoom);
                        break;

                    case 6:
                        System.out.println("Thank you for using Hotel Room Booking System!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }
            } while (choice != 6);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    // CREATE TABLES
    static void createTables(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String roomsTable = "CREATE TABLE IF NOT EXISTS rooms (room_id INT PRIMARY KEY AUTO_INCREMENT, room_number INT UNIQUE, room_type VARCHAR(50), price DOUBLE, status VARCHAR(20))";
        String customersTable = "CREATE TABLE IF NOT EXISTS customers (customer_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), phone VARCHAR(20))";
        String bookingsTable = "CREATE TABLE IF NOT EXISTS bookings (booking_id INT PRIMARY KEY AUTO_INCREMENT, customer_id INT, room_id INT, check_in DATE, check_out DATE, total_amount DOUBLE, status VARCHAR(20), FOREIGN KEY(customer_id) REFERENCES customers(customer_id), FOREIGN KEY(room_id) REFERENCES rooms(room_id))";
        stmt.executeUpdate(roomsTable);
        stmt.executeUpdate(customersTable);
        stmt.executeUpdate(bookingsTable);
        System.out.println("Tables Created Successfully!");
    }

    // INSERT ROOMS
    static void insertRooms(Connection con) throws SQLException {

        String sql = "INSERT IGNORE INTO rooms (room_number, room_type, price, status) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setInt(1, 101);
        ps.setString(2, "Single");
        ps.setDouble(3, 1500);
        ps.setString(4, "AVAILABLE");
        ps.executeUpdate();

        ps.setInt(1, 102);
        ps.setString(2, "Double");
        ps.setDouble(3, 2500);
        ps.setString(4, "AVAILABLE");
        ps.executeUpdate();

        ps.setInt(1, 103);
        ps.setString(2, "Deluxe");
        ps.setDouble(3, 3500);
        ps.setString(4, "AVAILABLE");
        ps.executeUpdate();

        ps.close();
    }

    // ROOM AVAILABILITY
    static void showAvailableRooms(Connection con) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE status = 'AVAILABLE'";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n===== AVAILABLE ROOMS =====");

        while (rs.next()) {
            System.out.println(
                    "Room: " + rs.getInt("room_number") +
                            " | Type: " + rs.getString("room_type") +
                            " | Price: ₹" + rs.getDouble("price")  );}
        rs.close();
        ps.close();
    }

    // BOOK ROOM
    static void bookRoom(Connection con, String name, String phone, int roomNumber, int days) throws SQLException {
        try {
            con.setAutoCommit(false);
            String roomSql = "SELECT room_id, price, status FROM rooms WHERE room_number = ? FOR UPDATE";
            PreparedStatement roomPs = con.prepareStatement(roomSql);
            roomPs.setInt(1, roomNumber);
            ResultSet roomRs = roomPs.executeQuery();

            if (!roomRs.next()) {
                System.out.println("Room does not exist!");
                con.rollback();
                return;
            }

            int roomId = roomRs.getInt("room_id");
            double price = roomRs.getDouble("price");
            String status = roomRs.getString("status");

            if (!status.equals("AVAILABLE")) {
                System.out.println("Room is not available!");
                con.rollback();
                return;
            }

            String customerSql = "INSERT INTO customers (name, phone) VALUES (?, ?)";

            PreparedStatement customerPs = con.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS);
            customerPs.setString(1, name);
            customerPs.setString(2, phone);
            customerPs.executeUpdate();
            ResultSet customerKeys = customerPs.getGeneratedKeys();
            customerKeys.next();
            int customerId = customerKeys.getInt(1);
            LocalDate checkIn = LocalDate.now();
            LocalDate checkOut = checkIn.plusDays(days);
            double totalAmount = price * days;

            String bookingSql = "INSERT INTO bookings (customer_id, room_id, check_in, check_out, total_amount, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement bookingPs = con.prepareStatement(bookingSql);
            bookingPs.setInt(1, customerId);
            bookingPs.setInt(2, roomId);
            bookingPs.setDate(3, Date.valueOf(checkIn));
            bookingPs.setDate(4, Date.valueOf(checkOut));
            bookingPs.setDouble(5, totalAmount);
            bookingPs.setString(6, "BOOKED");
            bookingPs.executeUpdate();
            String updateRoom = "UPDATE rooms SET status = 'BOOKED' WHERE room_id = ?";
            PreparedStatement updatePs = con.prepareStatement(updateRoom);
            updatePs.setInt(1, roomId);
            updatePs.executeUpdate();
            con.commit();

            System.out.println("\n===== BOOKING SUCCESSFUL =====");
            System.out.println("Customer Name : " + name);
            System.out.println("Room Number   : " + roomNumber);
            System.out.println("Check-In      : " + checkIn);
            System.out.println("Check-Out     : " + checkOut);
            System.out.println("Total Amount  : ₹" + totalAmount);
            System.out.println("Status        : BOOKED");

            roomRs.close();
            roomPs.close();
            customerKeys.close();
            customerPs.close();
            bookingPs.close();
            updatePs.close();

        } catch (SQLException e) {
            con.rollback();
            System.out.println("Booking failed!");
            System.out.println("Transaction Rolled Back!");
            e.printStackTrace();
        }
    }

    // CHECK-IN
    static void checkIn(Connection con, int roomNumber) throws SQLException {
        String sql = "UPDATE bookings b JOIN rooms r ON b.room_id = r.room_id SET b.status = 'CHECKED_IN' WHERE r.room_number = ? AND b.status = 'BOOKED'";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, roomNumber);
        int rows = ps.executeUpdate();
        if (rows > 0) {
            System.out.println("Customer Checked-In Successfully!");
        } else {
            System.out.println("No booking found for this room!");
        }
        ps.close();
    }

    // CHECK-OUT
    static void checkOut(Connection con, int roomNumber) throws SQLException {
        try {
            con.setAutoCommit(false);
            String sql = "UPDATE bookings b JOIN rooms r ON b.room_id = r.room_id SET b.status = 'CHECKED_OUT' WHERE r.room_number = ? AND b.status = 'CHECKED_IN'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, roomNumber);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                String roomSql = "UPDATE rooms SET status = 'AVAILABLE' WHERE room_number = ?";
                PreparedStatement roomPs = con.prepareStatement(roomSql);
                roomPs.setInt(1, roomNumber);
                roomPs.executeUpdate();
                con.commit();
                System.out.println("Customer Checked-Out Successfully!");
                roomPs.close();

            } else {
                con.rollback();
                System.out.println("Customer is not checked-in!");
            }
            ps.close();
        } catch (SQLException e) {
            con.rollback();
            System.out.println("Check-Out Failed!");
        }
    }

    // BILLING
    static void showBill(Connection con, int roomNumber) throws SQLException {

        String sql = "SELECT c.name, r.room_number, r.price, b.check_in, b.check_out, b.total_amount, b.status FROM bookings b JOIN customers c ON b.customer_id = c.customer_id JOIN rooms r ON b.room_id = r.room_id WHERE r.room_number = ? ORDER BY b.booking_id DESC LIMIT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, roomNumber);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("\n===== HOTEL BILL =====");
            System.out.println("Customer Name : " + rs.getString("name"));
            System.out.println("Room Number   : " + rs.getInt("room_number"));
            System.out.println("Room Price    : ₹" + rs.getDouble("price"));
            System.out.println("Check-In      : " + rs.getDate("check_in"));
            System.out.println("Check-Out     : " + rs.getDate("check_out"));
            System.out.println("Total Amount  : ₹" + rs.getDouble("total_amount"));
            System.out.println("Status        : " + rs.getString("status"));

        } else {
            System.out.println("No billing information found!");
        }
        rs.close();
        ps.close();
    }
}

