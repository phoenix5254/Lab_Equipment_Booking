package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import model.Enums.Roles;
import model.users.User;

public class UserManager extends User implements Serializable{
	private static final long serialVersionUID = 1L;
	// ── Socket and connection fields ───────────────────────────────────
    private Socket             socket       = null;
    private ObjectInputStream  objIs        = null;
    private ObjectOutputStream objOs        = null;
    private static Connection  myConn       = null;

    // ── Constructor - starts server, connects to DB, begins accept loop ───────
    public UserManager() {
            myConn = getDatabaseConnection();
            configureStreams();
    }

    // ── JDBC connection ───────────────────────────────────────────────────────
    public static Connection getDatabaseConnection() {
        String url = "jdbc:mysql://localhost:3306/equipmentbooking";
        if (myConn == null) {
            try {
                myConn = DriverManager.getConnection(url, "root", "");
                JOptionPane.showMessageDialog(null,
                    "DB Connection Established",
                    "CONNECTION STATUS",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                    "Could not connect to database\n" + ex,
                    "Connection Failure",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        return myConn;
    }
        // ── ObjectOutputStream FIRST then flush, THEN ObjectInputStream ───────
        private void configureStreams() {
            try {
                objOs = new ObjectOutputStream(socket.getOutputStream());
                objOs.flush();
                objIs = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ── Close all streams and socket ──────────────────────────────────────
        private void closeConnection() {
            try {
                if (objOs != null) objOs.close();
                if (objIs != null) objIs.close();
                if (socket != null) socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    // ── CREATE user record (JDBC) ─────────────────────────────────────────────
    public static int createUserRecord(User user) {
    	myConn = getDatabaseConnection();
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
            JOptionPane.showMessageDialog(null, "Insertion failed: " + e.getMessage(),
                "Insert Status", JOptionPane.ERROR_MESSAGE);
        }
        return affectedRows;
    }

    // ── UPDATE user (JDBC) ────────────────────────────────────────────────────
    public static int updateUser(User user) {
    	myConn = getDatabaseConnection();
        String sql = "UPDATE users SET firstName=?, lastName=?, password=?, salt=?, role=? "
                   + "WHERE userID=?";
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
            JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage(),
                "Update Status", JOptionPane.ERROR_MESSAGE);
        }
        return affectedRows;
    }

    // ── FIND user by ID (JDBC) ────────────────────────────────────────────────
    public static User findUserById(String userID) {
    	myConn = getDatabaseConnection();
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
//                    user.setSalt(result.getBytes("salt"));
                    user.setRole(Roles.valueOf(result.getString("role")));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Search failed: " + e.getMessage(),
                "Search Status", JOptionPane.ERROR_MESSAGE);
        }
        return user;
    }
    
    public static String getLabIdByUserId(String userId) {
        myConn = getDatabaseConnection();
        String sql = "SELECT labId FROM labs WHERE technicians = ?";
        System.out.println("User Manager: Technician Id Recieved: " + userId);
        
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("labId");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error getting technician labId: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }

 // ── DELETE user (JDBC) ────────────────────────────────────────────────────
    public static int deleteUser(String userID) {
    String deleteUserSql = "DELETE FROM users WHERE userID = ?";
    String updateLabSql = "UPDATE labs SET technicians = NULL WHERE technicians = ?";
    int userDeleted = 0;

    try {
        // Start a transaction so both happen or neither happens
        myConn.setAutoCommit(false);

        // 1. Update the labs first (Set technician to null if they exist there)
        try (PreparedStatement updateStmt = myConn.prepareStatement(updateLabSql)) {
            updateStmt.setString(1, userID);
            updateStmt.executeUpdate(); 
            // We don't check rows affected here because a user might not be assigned to a lab
        }

        // 2. Delete the user
        try (PreparedStatement deleteStmt = myConn.prepareStatement(deleteUserSql)) {
            deleteStmt.setString(1, userID);
            userDeleted = deleteStmt.executeUpdate();
        }

        // 3. Commit the changes
        if (userDeleted == 1) {
            myConn.commit();
            return 1;
        } else {
            myConn.rollback(); // User didn't exist
            return 0;
        }

    } catch (SQLException e) {
        try { myConn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        JOptionPane.showMessageDialog(null, "Delete failed: " + e.getMessage(),
                "Delete Status", JOptionPane.ERROR_MESSAGE);
        return 0;
    } finally {
        try { myConn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
    }
}

    public static void main(String[] args) {
        new UserManager();
    }

}
