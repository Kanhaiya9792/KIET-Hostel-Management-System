package com.kiet.hostel;

import java.sql.*;
import java.util.Scanner;

public class HostelManagementSystem {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to hostel_db successfully!");

            while (true) {
                System.out.println("\n========== KIET HOSTEL MANAGEMENT SYSTEM ==========");
                System.out.println("1. Insert 3 Hostel Student Records");
                System.out.println("2. Retrieve Students by Hostel (Fee > 50000)");
                System.out.println("3. Update Hostel Fee");
                System.out.println("4. Search Student and Update Room Number");
                System.out.println("5. Display Total Fee and Conditionally Delete Student");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                int choice = readInt();
                switch (choice) {
                    case 1 -> insertThreeStudents(con);
                    case 2 -> retrieveByHostel(con);
                    case 3 -> updateFee(con);
                    case 4 -> updateRoom(con);
                    case 5 -> showTotalAndDelete(con);
                    case 0 -> {
                        System.out.println("Thank you. Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.out.println("Check MySQL is running and DBConnection.java credentials are correct.");
        }
    }

    // (a) Insert at least three records using PreparedStatement.
    private static void insertThreeStudents(Connection con) {
        String sql = "INSERT INTO HostelStudent " +
                "(student_id, student_name, roll_number, hostel_name, room_number, fee) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Object[][] students = {
                {1001, "Aman Sharma", "24IT001", "Tagore Hostel", "T-101", 65000.0},
                {1002, "Rahul Verma", "24IT002", "Tagore Hostel", "T-102", 58000.0},
                {1003, "Rohit Kumar", "24IT003", "Raman Hostel", "R-201", 48000.0}
        };

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] s : students) {
                ps.setInt(1, (Integer) s[0]);
                ps.setString(2, (String) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setString(4, (String) s[3]);
                ps.setString(5, (String) s[4]);
                ps.setDouble(6, (Double) s[5]);

                try {
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Inserted student ID: " + s[0]);
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate student ID " + s[0] + ". Record skipped.");
                } catch (SQLException e) {
                    if ("23000".equals(e.getSQLState())) {
                        System.out.println("Duplicate/constraint issue for student ID " + s[0] + ". Record skipped.");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    // (b) Conditional retrieval and sorting.
    private static void retrieveByHostel(Connection con) {
        System.out.print("Enter hostel name: ");
        String hostel = sc.nextLine();

        String sql = "SELECT student_id, student_name, roll_number, hostel_name, room_number, fee " +
                "FROM HostelStudent WHERE hostel_name = ? AND fee > 50000 ORDER BY fee DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hostel);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printStudent(rs);
                }
                if (!found) {
                    System.out.println("No matching students found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Retrieval failed: " + e.getMessage());
        }
    }

    // (c) Update hostel fee.
    private static void updateFee(Connection con) {
        System.out.print("Enter student ID: ");
        int id = readInt();
        System.out.print("Enter new hostel fee: ");
        double fee = readDouble();

        if (fee < 0) {
            System.out.println("Fee cannot be negative.");
            return;
        }

        String sql = "UPDATE HostelStudent SET fee = ? WHERE student_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, fee);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Hostel fee updated successfully.");
            } else {
                System.out.println("Student record does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Fee update failed: " + e.getMessage());
        }
    }

    // (d) Search student, update room only if student exists, display updated details.
    private static void updateRoom(Connection con) {
        System.out.print("Enter student ID: ");
        int id = readInt();

        String selectSql = "SELECT * FROM HostelStudent WHERE student_id = ?";
        String updateSql = "UPDATE HostelStudent SET room_number = ? WHERE student_id = ?";

        try (PreparedStatement selectPs = con.prepareStatement(selectSql)) {
            selectPs.setInt(1, id);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Student record does not exist. Room was not updated.");
                    return;
                }

                System.out.println("Student found:");
                printStudent(rs);

                System.out.print("Enter new room number: ");
                String room = sc.nextLine();

                try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                    updatePs.setString(1, room);
                    updatePs.setInt(2, id);
                    updatePs.executeUpdate();
                }

                System.out.println("Room number updated. Updated student details:");
                try (PreparedStatement finalPs = con.prepareStatement(selectSql)) {
                    finalPs.setInt(1, id);
                    try (ResultSet updated = finalPs.executeQuery()) {
                        if (updated.next()) {
                            printStudent(updated);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Room update failed: " + e.getMessage());
        }
    }

    // (e) SUM(fee), then delete only when fee > 0.
    private static void showTotalAndDelete(Connection con) {
        String sumSql = "SELECT SUM(fee) AS total_fee FROM HostelStudent";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sumSql)) {
            if (rs.next()) {
                double total = rs.getDouble("total_fee");
                System.out.printf("Total hostel fee collected: Rs. %.2f%n", total);
            }
        } catch (SQLException e) {
            System.out.println("Could not calculate total fee: " + e.getMessage());
            return;
        }

        System.out.print("Enter student ID to delete: ");
        int id = readInt();

        String deleteSql = "DELETE FROM HostelStudent WHERE student_id = ? AND fee > 0";
        try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Student record deleted successfully (fee was greater than zero).");
            } else {
                System.out.println("Record cannot be deleted: student does not exist or fee is not greater than zero.");
            }
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }

    private static void printStudent(ResultSet rs) throws SQLException {
        System.out.printf(
                "ID: %d | Name: %s | Roll: %s | Hostel: %s | Room: %s | Fee: Rs. %.2f%n",
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("roll_number"),
                rs.getString("hostel_name"),
                rs.getString("room_number"),
                rs.getDouble("fee")
        );
    }

    private static int readInt() {
        while (true) {
            try {
                int value = Integer.parseInt(sc.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid integer: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
