package JDBCTask;
import java.sql.*;
import java.util.Scanner;

public class StudentManagementSystem {

    static final String URL = "jdbc:mysql://localhost:3306/student_db";
    static final String USER = "root";
    static final String PASSWORD = "Dhaya@21052004";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Database Connected Successfully!");
            createTables(con);
            insertCourses(con);
            int choice;

            do {

                System.out.println("\n===== STUDENT MANAGEMENT SYSTEM =====");
                System.out.println("1. Student Registration");
                System.out.println("2. Course Enrollment");
                System.out.println("3. Attendance Management");
                System.out.println("4. Marks Entry");
                System.out.println("5. Result Generation");
                System.out.println("6. View Students");
                System.out.println("7. Delete Student");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1:

                        System.out.print("Enter student name: ");
                        String name = sc.nextLine();

                        System.out.print("Enter email: ");
                        String email = sc.nextLine();

                        System.out.print("Enter phone: ");
                        String phone = sc.nextLine();

                        registerStudent(con, name, email, phone);

                        break;

                    case 2:

                        System.out.print("Enter student ID: ");
                        int studentId = sc.nextInt();
                        System.out.print("Enter course ID: ");
                        int courseId = sc.nextInt();
                        enrollStudent(con, studentId, courseId);

                        break;

                    case 3:

                        System.out.print("Enter student ID: ");
                        int attendanceStudentId = sc.nextInt();

                        System.out.print("Enter course ID: ");
                        int attendanceCourseId = sc.nextInt();

                        System.out.print("Enter attendance percentage: ");
                        double attendancePercentage = sc.nextDouble();

                        updateAttendance(con, attendanceStudentId, attendanceCourseId, attendancePercentage);

                        break;

                    case 4:

                        System.out.print("Enter student ID: ");
                        int marksStudentId = sc.nextInt();
                        System.out.print("Enter course ID: ");
                        int marksCourseId = sc.nextInt();
                        System.out.print("Enter marks: ");
                        double marks = sc.nextDouble();
                        enterMarks(con, marksStudentId, marksCourseId, marks);
                        break;

                    case 5:

                        System.out.print("Enter student ID: ");
                        int resultStudentId = sc.nextInt();
                        generateResult(con, resultStudentId);
                        break;

                    case 6:
                        viewStudents(con);
                        break;

                    case 7:
                        System.out.print("Enter student ID to delete: ");
                        int deleteStudentId = sc.nextInt();
                        deleteStudent(con, deleteStudentId);
                        break;

                    case 8:
                        System.out.println("Thank you for using Student Management System!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 8);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    // CREATE TABLES
    static void createTables(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String studentsTable = "CREATE TABLE IF NOT EXISTS students (student_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), email VARCHAR(100), phone VARCHAR(20))";
        String coursesTable = "CREATE TABLE IF NOT EXISTS courses (course_id INT PRIMARY KEY AUTO_INCREMENT, course_name VARCHAR(100), duration VARCHAR(50))";
        String enrollmentsTable = "CREATE TABLE IF NOT EXISTS enrollments (enrollment_id INT PRIMARY KEY AUTO_INCREMENT, student_id INT, course_id INT, enrollment_date DATE, FOREIGN KEY(student_id) REFERENCES students(student_id), FOREIGN KEY(course_id) REFERENCES courses(course_id))";
        String attendanceTable = "CREATE TABLE IF NOT EXISTS attendance (attendance_id INT PRIMARY KEY AUTO_INCREMENT, student_id INT, course_id INT, attendance_percentage DOUBLE, FOREIGN KEY(student_id) REFERENCES students(student_id), FOREIGN KEY(course_id) REFERENCES courses(course_id))";
        String marksTable = "CREATE TABLE IF NOT EXISTS marks (mark_id INT PRIMARY KEY AUTO_INCREMENT, student_id INT, course_id INT, marks DOUBLE, FOREIGN KEY(student_id) REFERENCES students(student_id), FOREIGN KEY(course_id) REFERENCES courses(course_id))";

        stmt.executeUpdate(studentsTable);
        stmt.executeUpdate(coursesTable);
        stmt.executeUpdate(enrollmentsTable);
        stmt.executeUpdate(attendanceTable);
        stmt.executeUpdate(marksTable);
        System.out.println("Tables Created Successfully!");
    }

    // INSERT COURSES

    static void insertCourses(Connection con) throws SQLException {

        String sql = "INSERT IGNORE INTO courses (course_id, course_name, duration) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, 1);
        ps.setString(2, "Java");
        ps.setString(3, "6 Months");
        ps.executeUpdate();

        ps.setInt(1, 2);
        ps.setString(2, "Python");
        ps.setString(3, "6 Months");
        ps.executeUpdate();

        ps.setInt(1, 3);
        ps.setString(2, "SQL");
        ps.setString(3, "3 Months");
        ps.executeUpdate();
        ps.close();
    }

    // STUDENT REGISTRATION

    static void registerStudent(Connection con, String name, String email, String phone) throws SQLException {

        String sql = "INSERT INTO students (name, email, phone) VALUES (?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, phone);
        ps.executeUpdate();

        System.out.println("Student Registered Successfully!");
        ps.close();
    }

    // COURSE ENROLLMENT WITH TRANSACTION
    static void enrollStudent(Connection con, int studentId, int courseId) throws SQLException {
        try {
            con.setAutoCommit(false);
            String studentSql = "SELECT student_id FROM students WHERE student_id = ?";
            PreparedStatement studentPs = con.prepareStatement(studentSql);
            studentPs.setInt(1, studentId);
            ResultSet studentRs = studentPs.executeQuery();

            if (!studentRs.next()) {
                System.out.println("Student not found!");
                con.rollback();
                return;
            }

            String courseSql = "SELECT course_id FROM courses WHERE course_id = ?";
            PreparedStatement coursePs = con.prepareStatement(courseSql);
            coursePs.setInt(1, courseId);
            ResultSet courseRs = coursePs.executeQuery();

            if (!courseRs.next()) {
                System.out.println("Course not found!");
                con.rollback();
                return;
            }

            String enrollmentSql = "INSERT INTO enrollments (student_id, course_id, enrollment_date) VALUES (?, ?, CURDATE())";
            PreparedStatement enrollmentPs = con.prepareStatement(enrollmentSql);
            enrollmentPs.setInt(1, studentId);
            enrollmentPs.setInt(2, courseId);
            enrollmentPs.executeUpdate();
            con.commit();

            System.out.println("Student Enrolled Successfully!");
            studentRs.close();
            studentPs.close();
            courseRs.close();
            coursePs.close();
            enrollmentPs.close();

        } catch (SQLException e) {
            con.rollback();
            System.out.println("Enrollment Failed!");
            System.out.println("Transaction Rolled Back!");
        }
    }

    // ATTENDANCE MANAGEMENT
    static void updateAttendance(Connection con, int studentId, int courseId, double percentage) throws SQLException {
        String checkSql = "SELECT attendance_id FROM attendance WHERE student_id = ? AND course_id = ?";
        PreparedStatement checkPs = con.prepareStatement(checkSql);
        checkPs.setInt(1, studentId);
        checkPs.setInt(2, courseId);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next()) {
            String updateSql = "UPDATE attendance SET attendance_percentage = ? WHERE student_id = ? AND course_id = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setDouble(1, percentage);
            updatePs.setInt(2, studentId);
            updatePs.setInt(3, courseId);
            updatePs.executeUpdate();

            System.out.println("Attendance Updated Successfully!");
            updatePs.close();

        } else {
            String insertSql = "INSERT INTO attendance (student_id, course_id, attendance_percentage) VALUES (?, ?, ?)";
            PreparedStatement insertPs = con.prepareStatement(insertSql);
            insertPs.setInt(1, studentId);
            insertPs.setInt(2, courseId);
            insertPs.setDouble(3, percentage);
            insertPs.executeUpdate();

            System.out.println("Attendance Added Successfully!");
            insertPs.close();
        }
        rs.close();
        checkPs.close();
    }

    // MARKS ENTRY
    static void enterMarks(Connection con, int studentId, int courseId, double marks) throws SQLException {
        String checkSql = "SELECT mark_id FROM marks WHERE student_id = ? AND course_id = ?";
        PreparedStatement checkPs = con.prepareStatement(checkSql);
        checkPs.setInt(1, studentId);
        checkPs.setInt(2, courseId);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next()) {
            String updateSql = "UPDATE marks SET marks = ? WHERE student_id = ? AND course_id = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setDouble(1, marks);
            updatePs.setInt(2, studentId);
            updatePs.setInt(3, courseId);
            updatePs.executeUpdate();

            System.out.println("Marks Updated Successfully!");
            updatePs.close();

        } else {
            String insertSql = "INSERT INTO marks (student_id, course_id, marks) VALUES (?, ?, ?)";
            PreparedStatement insertPs = con.prepareStatement(insertSql);
            insertPs.setInt(1, studentId);
            insertPs.setInt(2, courseId);
            insertPs.setDouble(3, marks);
            insertPs.executeUpdate();

            System.out.println("Marks Entered Successfully!");
            insertPs.close();
        }
        rs.close();
        checkPs.close();
    }

    // RESULT GENERATION USING JOINS
    static void generateResult(Connection con, int studentId) throws SQLException {
        String sql = "SELECT s.name, c.course_name, a.attendance_percentage, m.marks FROM students s JOIN enrollments e ON s.student_id = e.student_id JOIN courses c ON e.course_id = c.course_id LEFT JOIN attendance a ON s.student_id = a.student_id AND c.course_id = a.course_id LEFT JOIN marks m ON s.student_id = m.student_id AND c.course_id = m.course_id WHERE s.student_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();

        System.out.println("\n========== STUDENT RESULT ==========");
        boolean found = false;

        while (rs.next()) {

            found = true;
            double marks = rs.getDouble("marks");
            double attendance = rs.getDouble("attendance_percentage");
            String result;

            if (marks >= 40 && attendance >= 75) {
                result = "PASS";
            } else {
                result = "FAIL";
            }

            System.out.println("\nStudent Name : " + rs.getString("name"));
            System.out.println("Course       : " + rs.getString("course_name"));
            System.out.println("Attendance   : " + attendance + "%");
            System.out.println("Marks        : " + marks);
            System.out.println("Result       : " + result);
        }
        if (!found) {
            System.out.println("Student result not found!");
        }
        System.out.println("====================================");
        rs.close();
        ps.close();
    }

    // VIEW STUDENTS USING JOIN
    static void viewStudents(Connection con) throws SQLException {
        String sql = "SELECT s.student_id, s.name, s.email, s.phone, c.course_name FROM students s LEFT JOIN enrollments e ON s.student_id = e.student_id LEFT JOIN courses c ON e.course_id = c.course_id ORDER BY s.student_id";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n========== STUDENT LIST ==========");

        while (rs.next()) {
            System.out.println("Student ID : " + rs.getInt("student_id"));
            System.out.println("Name       : " + rs.getString("name"));
            System.out.println("Email      : " + rs.getString("email"));
            System.out.println("Phone      : " + rs.getString("phone"));
            System.out.println("Course     : " + rs.getString("course_name"));
            System.out.println("----------------------------------");
        }
        rs.close();
        ps.close();
    }

    // DELETE STUDENT
    static void deleteStudent(Connection con, int studentId) throws SQLException {
        try {
            con.setAutoCommit(false);
            String marksSql = "DELETE FROM marks WHERE student_id = ?";
            PreparedStatement marksPs = con.prepareStatement(marksSql);
            marksPs.setInt(1, studentId);
            marksPs.executeUpdate();

            String attendanceSql = "DELETE FROM attendance WHERE student_id = ?";
            PreparedStatement attendancePs = con.prepareStatement(attendanceSql);
            attendancePs.setInt(1, studentId);
            attendancePs.executeUpdate();

            String enrollmentSql = "DELETE FROM enrollments WHERE student_id = ?";
            PreparedStatement enrollmentPs = con.prepareStatement(enrollmentSql);
            enrollmentPs.setInt(1, studentId);
            enrollmentPs.executeUpdate();

            String studentSql = "DELETE FROM students WHERE student_id = ?";
            PreparedStatement studentPs = con.prepareStatement(studentSql);
            studentPs.setInt(1, studentId);
            int rows = studentPs.executeUpdate();

            if (rows > 0) {
                con.commit();
                System.out.println("Student Deleted Successfully!");

            } else {
                con.rollback();
                System.out.println("Student Not Found!");
            }

            marksPs.close();
            attendancePs.close();
            enrollmentPs.close();
            studentPs.close();

        } catch (SQLException e) {
            con.rollback();
            System.out.println("Delete Failed!");
            System.out.println("Transaction Rolled Back!");
        }}}

