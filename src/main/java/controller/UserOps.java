package controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;
//import controller.ReservationManager.ClientHandler;
import model.Enums.Roles;
import model.users.User;

public class UserManager extends User{

    // ── Server socket and connection fields ───────────────────────────────────
    private ServerSocket       serverSocket = null;
    private Socket             socket       = null;
    private ObjectInputStream  objIs        = null;
    private ObjectOutputStream objOs        = null;
    private static Connection  dbConn       = null;

    // ── Constructor - starts server, connects to DB, begins accept loop ───────
    public UserManager() {
        try {
            serverSocket = new ServerSocket(8888, 50);
            dbConn = getDatabaseConnection();

            System.out.println("USER MANAGER SERVER STARTED");

            waitForRequests();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Accept loop - waits for client connections, spawns thread per client ──
    private void waitForRequests() {
        try {
            while (true) {
                socket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (EOFException ex) {
            System.out.println("Client has terminated connections with the server");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── JDBC connection ───────────────────────────────────────────────────────
    private static Connection getDatabaseConnection() {
        String url = "jdbc:mysql://localhost:3306/equipmentbooking";
        if (dbConn == null) {
            try {
                dbConn = DriverManager.getConnection(url, "root", "");
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
        return dbConn;
    }

    // =========================================================================
    //  ClientHandler - inner class, one per connected client, runs on its thread
    // =========================================================================
    public static class ClientHandler implements Runnable {

        private Socket             socket = null;//As many clients created are on a new socket, one server multiple threads
        private ObjectInputStream  objIs  = null;//facilitates transfer of objects
        private ObjectOutputStream objOs  = null;//facilitates transfer of objects

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            dbConn = getDatabaseConnection();
            configureStreams();

            try {
                String action = (String) objIs.readObject();

                if (action.equals("Create User")) {
                    User user = (User) objIs.readObject();
                    System.out.println("SERVER: CREATING USER RECEIVED FROM CLIENT");
                    int rows = createUserRecord(user);
                    System.out.println("SERVER: USER CREATED SUCCESSFULLY");
                    objOs.writeObject(rows == 1);

                } else if (action.equals("Find User")) {
                    String userID = (String) objIs.readObject();
                    System.out.println("SERVER: FINDING USER " + userID);
                    User user = findUserById(userID);
                    System.out.println("SERVER: SENDING USER TO CLIENT");
                    objOs.writeObject(user);

                } else if (action.equals("Update User")) {
                    User user = (User) objIs.readObject();
                    System.out.println("SERVER: UPDATING USER " + user.getUserID());
                    int rows = updateUser(user);
                    System.out.println("SERVER: USER UPDATED SUCCESSFULLY");
                    objOs.writeObject(rows == 1);

                } else if (action.equals("Delete User")) {
                    String userID = (String) objIs.readObject();
                    System.out.println("SERVER: DELETING USER " + userID);
                    int rows = deleteUser(userID);
                    System.out.println("SERVER: USER DELETED FROM DB");
                    objOs.writeObject(rows == 1);

                } else {
                    JOptionPane.showMessageDialog(null,
                        "SERVER HAS NO RESPONSE FOR THIS ACTION");
                }

                objOs.flush();

            } catch (EOFException ex) {
                System.out.println("Client has terminated connections with the server");
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
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

    } // END ClientHandler

    // ── CREATE user record (JDBC) ─────────────────────────────────────────────
    public static int createUserRecord(User user) {
        String sql = "INSERT INTO users (userID, firstName, lastName, email, password, role) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        int affectedRows = 0;
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
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
        String sql = "UPDATE users SET firstName=?, lastName=?, password=?, salt=?, role=? "
                   + "WHERE userID=?";
        int affectedRows = 0;
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
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
        String sql = "SELECT * FROM users WHERE userID = ?";
        User user = null;
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
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
            JOptionPane.showMessageDialog(null, "Search failed: " + e.getMessage(),
                "Search Status", JOptionPane.ERROR_MESSAGE);
        }
        return user;
    }

    // ── DELETE user (JDBC) ────────────────────────────────────────────────────
    public static int deleteUser(String userID) {
        String sql = "DELETE FROM users WHERE userID = ?";
        int affectedRows = 0;
        try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            stmt.setString(1, userID);
            affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Delete failed: " + e.getMessage(),
                "Delete Status", JOptionPane.ERROR_MESSAGE);
        }
        return affectedRows;
    }

    public static void main(String[] args) {
        new UserManager();
    }

}
