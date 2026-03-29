package com_java_users;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

import javax.swing.JOptionPane;

import com_java_users.Enums.Roles;

public class Driver {
    static Connection myConn = null;
    static ResultSet myRs = null;

    static void connectDatabse() {
        String url = "jdbc:mysql://localhost:3306/equipmentbooking";
        try {
            myConn = DriverManager.getConnection(url, "root", "");
        } catch (SQLException sql) {
            // JOptionPane.showMessageDialog(null,sql.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to connect to local server: " + sql.getMessage(),
                    "JDBC Connection Status", JOptionPane.ERROR_MESSAGE);
        }
        if (myConn != null) {
            JOptionPane.showMessageDialog(null, "Connected to Database", "Database Connection Status",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    static void createUser(User user) {
        String sql = "INSERT INTO users (userID, firstName, lastName, email, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserID());
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getLastName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPassword());
            pstmt.setString(6, user.getRole().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Insertion failed: " + e.getMessage(), "Insert Status",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    static void updateUser(User userLookup) {
         StringJoiner sj = new StringJoiner(",");
        User user = findUserById(userLookup);
        if (user == null)
            return;
        
        for (Field column : user.getClass().getDeclaredFields()) {
            try {
                column.setAccessible(true);
                if(column.getName().equals("salt")){continue;}
                Object newAttribute = column.get(userLookup);
                Object currAttribute = column.get(user);

                if (currAttribute != newAttribute) {//change attributes
                    if(userLookup.getRole()==(user.getRole())&& user.getUserID()==userLookup.getUserID()||user.getRole()==Roles.ADMIN && user.getUserID()!=userLookup.getUserID()){ //change attributes except role ans id
                        user=userLookup;
                    }
                    if(userLookup.getRole()!=user.getRole()|| user.getUserID()!=userLookup.getUserID()){
                       if(user.getRole()!=Roles.ADMIN && user.getUserID()==userLookup.getUserID()||user.getRole()!=Roles.ADMIN && user.getUserID()!=userLookup.getUserID()){ 
                       //Only admins can change user role
                        JOptionPane.showMessageDialog(null, "You are not allowed to change user role and/or user ID", "Update Status",JOptionPane.ERROR_MESSAGE);return;
                        }
                    }
                }
                        sj.add(column.getName()+"='"+newAttribute+"'");
                    
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Identifying fields to be changed failed:" + e.getMessage());
            }
        }

        if (sj.length() == 0)
            return; // Nothing to update

        // Fix 3: Assemble the final query string carefully
        String sql = "UPDATE users SET "+ sj.toString() + " WHERE userID = "+"'"+userLookup.getUserID()+"'";

        System.out.println("Executing SQL: " + sql);

        try (Statement mystmt = myConn.createStatement()) { {
             mystmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage());
        }
    }

    static User findUserById(User userLookup) {
        User userObj = new User();
        try (Statement myStmt = myConn.createStatement()) {
            myRs = myStmt.executeQuery("SELECT * FROM users WHERE userID='" + userLookup.getUserID() + "'");
            if (myRs.next()) {// found user
                userObj.setUserID(myRs.getString("userID"));
                userObj.setFirstName(myRs.getString("firstName"));
                userObj.setLastName(myRs.getString("lastName"));
                userObj.setEmail(myRs.getString("email"));
                userObj.setPassword(myRs.getString("password"));
                userObj.setRole(Roles.valueOf(myRs.getString("role")));// disable functionality if not admin in GUI
                return userObj;
            } else {
                userObj = null;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Lookup failed: " + e.getMessage(), "Lookup Status",
                    JOptionPane.ERROR_MESSAGE);
        }
        return userObj;
    }
    static void deleteUser(User user) {
        String sql = "DELETE FROM users WHERE userID = ?";
        try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Deletion failed: " + e.getMessage(), "Delete Status",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        connectDatabse();
        User user = new User("l1", "higher ", "dunbarton", "jdoe@my.com", "password123", Roles.ADMIN);
        // insertUser(user);
        updateUser(user);
    }
}