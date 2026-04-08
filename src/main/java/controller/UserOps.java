package controller;


import java.sql.*;

import model.Roles;

import java.util.Arrays;

import javax.swing.JOptionPane;

import model.User;

public class UserOps {
	 private static Connection myConn = null;

	    public UserOps() {
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
	    
	    /*public int createReservation(String reservationNum,String userId,String resourceID,Time startTime,Time endTime,String status) {

	        String sql = "INSERT INTO reservations "
	                   + "(reservationNum, userID, resourceID, Date, startTime, endTime, status) "
	                   + "VALUES (?, ?, ?, NOW(), ?, ?, ?)";

	        int affectedRows = 0;

	        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

	            stmt.setString(1, reservationNum);
	            stmt.setString(2, userId);
	            stmt.setString(3, resourceID);
	            stmt.setTime(4, startTime);
	            stmt.setTime(5, endTime);
	            stmt.setString(6, status);

	            affectedRows = stmt.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return affectedRows;
	    }*/


	    public static Connection getConnection() {

	        String url = "jdbc:mysql://localhost:3307/theclebdb";

	        if (myConn == null) {
	            try {
	                myConn = DriverManager.getConnection(url, "root", "usbw");
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }

	        return myConn;
	    }
}


