package controller;

import java.sql.*;
import javax.swing.JOptionPane;

import model.Enums.Roles;
import model.users.User;

public class UserManager {
    private static Connection myConn = null;

    static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/equipmentbooking";

        if (myConn == null) {
            try {
                myConn = DriverManager.getConnection(url, "root", "");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return myConn;
    }

    public UserManager() {
        myConn = getConnection();
    }

    public int createUserRecord(User user) {

        String sql = "INSERT INTO users (userID, firstName, lastName, email, password, role) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        int affectedRows = 0;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setString(1, user.getUserID());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, new String(user.getPassword()));
            stmt.setString(6, user.getRole().name());
            affectedRows = stmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Insertion failed: " + e.getMessage(), "Insert Status",
                    JOptionPane.ERROR_MESSAGE);
        }

        return affectedRows;//
    }

    public int updateUser(User user) {

        String sql = "UPDATE users SET firstName=?, lastName=?, password=?, salt=?, role=? "+ "WHERE userID=?";
        int affectedRows = 0;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, new String(user.getPassword()));
            stmt.setBytes(4, user.getSalt());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getUserID());

            affectedRows = stmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Update failed: " + e.getMessage(),
                    "Update Status",
                    JOptionPane.ERROR_MESSAGE);
        }

        return affectedRows;
    }

    public User findUserById(String userID) {

        String sql = "SELECT * FROM users WHERE userID = ?";

        User user = null;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, userID);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    user = new User();
                    user.setUserID(result.getString("userID"));
                    user.setFirstName(result.getString("firstName"));
                    user.setLastName(result.getString("lastName"));
                    user.setPassword(result.getString("password"));
                    user.setSalt(result.getBytes("salt"));
                    user.setRole(Roles.valueOf(result.getString("role")));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Search failed: " + e.getMessage(),
                    "Search Status",
                    JOptionPane.ERROR_MESSAGE);
        }
        return user;
    }

    public int deleteUser(String userID) {
        String sql = "DELETE FROM users WHERE userID = ?";
        int affectedRows = 0;
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, userID);
            affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Delete failed: " + e.getMessage(),
                    "Delete Status",
                    JOptionPane.ERROR_MESSAGE);
        }
        return affectedRows;
    }

    //only admins may update users role function return true if admin
   public boolean readRole(String userID) {
        String sql = "SELECT role FROM users WHERE userID = ?";
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, userID);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getString("role").equals("ADMIN");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Search failed: " + e.getMessage(),
                    "Search Status",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}
