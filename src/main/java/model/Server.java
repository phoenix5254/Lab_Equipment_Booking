package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.ReservationOps;
import controller.ResourcesManager;
import controller.UserOps;
import model.Enums.EquipStatus;
import model.Enums.Roles;
import model.resource.Equipment;
import model.resource.Lab;
import model.resource.Reservation;
import model.resource.SeatRecord;
import model.users.User;

/*
 * ============================================================
 *  CLEB Multi-Threaded TCP/IP Server
 *
 * ============================================================
 *
 *
 *   TCP/IP blocking sockets via ServerSocket(8888) + Socket.accept()
 *   Thread pool of 30 workers via Executors.newFixedThreadPool(30)
 *           Supports 10-30 concurrent LAN clients simultaneously
 *   Real-time push: after approve/reject/cancel the server writes
 *           PUSH_UPDATE ResponseEnvelope to ALL connected clients via
 *           pushUpdateToAllClients() using ConcurrentHashMap registry
 *  All messages wrapped in RequestEnvelope / ResponseEnvelope
 *           with UUID correlation IDs matching request to response
 * ONLY the server communicates with the database.
 *
 *  THREADING:
 *  Main thread   - serverSocket.accept() loop only, no processing
 *  Pool threads  - each ClientHandler runs on its own pool thread
 *  Push thread   - whichever pool thread processed approve/reject/cancel
 *                  calls pushUpdateToAllClients() to notify all clients
 *
 */
public class Server implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Loggers (Log4J2 - rolling by size and by day, separate audit log) ──────
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static final Logger audit  = LogManager.getLogger("AUDIT");

    // ── Server configuration ───────────────────────────────────────────────────
    private static final int PORT      = 8888;
    private static final int POOL_SIZE = 30; // supports 10-30 concurrent LAN clients

    // ── JDBC connection - ONLY the server connects
    // Used for login (salt:hash split) 
    private static Connection myConn = null;

    // ── Thread pool reused per connection ──────────
    private static ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);

    // ── Push notification registry
    // Maps clientId -> ObjectOutputStream so server can push to all clients
    // ConcurrentHashMap: thread-safe for concurrent add/remove by pool threads
    private static final Map<String, ObjectOutputStream> connectedClients =
            new ConcurrentHashMap<>();

    private ServerSocket serverSocket = null;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("SERVER STARTED ON PORT {}", PORT);
            logger.info("Thread pool: {} workers (supports {} concurrent clients)", POOL_SIZE, POOL_SIZE);
            // Establish JDBC connection - only server does this
            getDatabaseConnection();
            // Block on accept loop - main thread never does request processing
            waitForRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── JDBC connection 
    private static Connection getDatabaseConnection() {
        if (myConn == null) {
            try {
                // Database: equipmentbooking  
                String url = "jdbc:mysql://localhost:3306/equipmentbooking";
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

    // ── Accept loop - runs on main thread only, no request processing
    private void waitForRequests() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("CLIENT CONNECTED: {}", socket.getRemoteSocketAddress());
                // Hand off to pool immediately - main thread returns to accept()
                threadPool.submit(new ClientHandler(socket));
            }
        } catch (EOFException ex) {
            logger.warn("Client terminated connection with the server");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Push real-time update to ALL connected clients
    // Called after approve / reject / cancel on whichever pool thread processed it.
    // Writes PUSH_UPDATE ResponseEnvelope with a fresh UUID to every registered client.
    private static void pushUpdateToAllClients(Reservation reservation, String message) {
        // Push envelope gets a new UUID - no matching request triggered this
        ResponseEnvelope push = new ResponseEnvelope("PUSH_UPDATE", message, reservation);
        logger.info("PUSHING UPDATE TO {} CLIENTS: {}", connectedClients.size(), message);
        for (Map.Entry<String, ObjectOutputStream> entry : connectedClients.entrySet()) {
            try {
                // Synchronized per stream - other threads may push simultaneously
                synchronized (entry.getValue()) {
                    entry.getValue().writeObject(push);
                    entry.getValue().flush();
                    entry.getValue().reset();
                }
            } catch (IOException e) {
                logger.warn("PUSH FAILED for: {} - removing from registry", entry.getKey());
                connectedClients.remove(entry.getKey());
            }
        }
    }

    // ==========================================================================
    //  ClientHandler - inner Runnable, one instance per connected client.
    //  Runs entirely on a pool thread - never on the main thread.
    // ==========================================================================
    private static class ClientHandler implements Runnable {

        private Socket             socket   = null;
        private ObjectInputStream  objIs    = null; // reads RequestEnvelopes from client
        private ObjectOutputStream objOs    = null; // writes ResponseEnvelopes to client
        private String             clientId;        // remote address used as push registry key

        public ClientHandler(Socket socket) {
            this.socket   = socket;
            this.clientId = socket.getRemoteSocketAddress().toString();
        }

        @Override
        public void run() {
            configureStreams();
            handleRequests();
        }

        // ── Open streams: ObjectOutputStream FIRST then flush, THEN ObjectInputStream
        // If both sides open ObjectInputStream first they deadlock on the stream header.
        private void configureStreams() {
            try {
                objOs = new ObjectOutputStream(socket.getOutputStream());
                objOs.flush(); // send header bytes so client can open its input stream
                objIs = new ObjectInputStream(socket.getInputStream());
                connectedClients.put(clientId, objOs); // register for push notifications
                logger.info("STREAMS CONFIGURED: {}", clientId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ── Main request loop - blocks on readObject() between requests
        private void handleRequests() {
            try {
                while (true) {
                    RequestEnvelope request = (RequestEnvelope) objIs.readObject();
                    logger.info("REQUEST [{}]: {}", clientId, request.getAction());

                    ResponseEnvelope response = dispatch(request);

                    synchronized (objOs) {
                        objOs.writeObject(response);
                        objOs.flush();
                        objOs.reset();
                    }

                    if ("Logout".equals(request.getAction())) {
                        logger.info("CLIENT LOGGED OUT: {}", clientId);
                        audit.info("LOGOUT | clientId={}", clientId);
                        break;
                    }
                }
            } catch (EOFException ex) {
                logger.warn("CLIENT DISCONNECTED: {}", clientId);
            } catch (IOException | ClassNotFoundException | ClassCastException ex) {
                ex.printStackTrace();
            } finally {
                connectedClients.remove(clientId);
                closeConnection();
            }
        }

        // ── Dispatcher - routes each action to the correct instance method call
        private ResponseEnvelope dispatch(RequestEnvelope request) {

            UUID   id     = request.getCorrelationId();
            String action = request.getAction();
            Object data   = request.getPayload();

            ResponseEnvelope re = null;

            try { switch (action) {

            // ==================================================================
            //  AUTH - Login 
            // ==================================================================

            case "Login": {
                // Reads stored "saltBase64:hashBase64" from password column,
                // splits it and calls User.verifyPassword() to authenticate.
                User user = (User) data;
                String sql = "SELECT * FROM users WHERE userID = ?";
                try (PreparedStatement mystmt = myConn.prepareStatement(sql)) {
                    mystmt.setString(1, user.getUserID());
                    ResultSet myRs = mystmt.executeQuery();
                    if (myRs.next()) {
                        User found = new User();
                        found.setUserID(myRs.getString("userID"));
                        found.setFirstName(myRs.getString("firstName"));
                        found.setLastName(myRs.getString("lastName"));
                        found.setEmail(myRs.getString("email"));
                        found.setRole(Roles.valueOf(myRs.getString("role")));
                        // Split "saltBase64:hashBase64" stored in password column
                        String[] parts      = myRs.getString("password").split(":");
                        byte[]   storedSalt = Base64.getDecoder().decode(parts[0]);
                        String   storedHash = parts[1];
                        // verifyPassword() from User.java hashes input with stored salt and compares
                        if (found.verifyPassword(user.getPassword(), storedHash, storedSalt)) {
                            found.setPassword(storedHash);
                            found.setSalt(storedSalt);
                            logger.info("LOGIN SUCCESS: {} role={}", found.getUserID(), found.getRole().name());
                            audit.info("LOGIN SUCCESS | userID={} role={}", found.getUserID(), found.getRole().name());
                            re = new ResponseEnvelope(id, action, true, "Login successful", found);
                        } else {
                            logger.warn("LOGIN FAILED: {}", user.getUserID());
                            audit.info("LOGIN FAILED | userID={}", user.getUserID());
                            re = new ResponseEnvelope(id, action, false, "Invalid User ID or Password", null);
                        }
                    } else {
                        logger.warn("LOGIN FAILED - user not found: {}", user.getUserID());
                        re = new ResponseEnvelope(id, action, false, "Invalid User ID or Password", null);
                    }
                } catch (SQLException e) {
                    logger.error("Login DB error for {}: {}", user.getUserID(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Register": {
                // UserOps.createUserRecord (JDBC)
                // Salt embedded in password column as "saltBase64:hashBase64"
                User user = (User) data;
                UserOps userOps = new UserOps();
                try {
                    String saltBase64 = Base64.getEncoder().encodeToString(user.getSalt());
                    user.setPassword(saltBase64 + ":" + user.getPassword());
                    int rows = userOps.createUserRecord(user);
                    logger.info("REGISTER: {} role={}", user.getUserID(), user.getRole().name());
                    audit.info("REGISTER | userID={} role={}", user.getUserID(), user.getRole().name());
                    re = new ResponseEnvelope(id, action, rows == 1,
                        rows == 1 ? "Registration successful" : "Registration failed", null);
                } catch (Exception e) {
                    logger.error("Register error for {}: {}", user.getUserID(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false,
                        "Registration failed - ID or email may already exist", null);
                }
                break;
            }

            case "Logout": {
                re = new ResponseEnvelope(id, action, true, "Logged out", null);
                break;
            }

            // ==================================================================
            //  RESERVATION - ReservationOps (Hibernate)
            // ==================================================================

            case "Add Reservation": {
                // ReservationOps.createReservation (Hibernate)
                Reservation res = (Reservation) data;
                ReservationOps resOps = new ReservationOps();
                try {
                    List<String> seatIds = new ArrayList<>();
                    if (res.getSeats() != null) {
                        for (SeatRecord s : res.getSeats()) seatIds.add(s.getSeatID());
                    }
                    resOps.createReservation(
                        res.getUserId(),
                        res.getLab() != null ? res.getLab().getLabId() : null,
                        seatIds,
                        res.getReservationDate(),
                        res.getStartTime(),
                        res.getEndTime(),
                        "PENDING"
                    );
                    logger.info("ADD RESERVATION for userId={}", res.getUserId());
                    audit.info("ADD RESERVATION | userId={}", res.getUserId());
                    re = new ResponseEnvelope(id, action, true,
                        "Reservation submitted - awaiting approval", null);
                } catch (Exception e) {
                    logger.error("Error adding reservation for {}: {}", res.getUserId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Find Reservation": {
                // ReservationOps.viewReservation (Hibernate)
                int reservationNum = (Integer) data;
                ReservationOps resOps = new ReservationOps();
                try {
                    Reservation res = resOps.viewReservation(reservationNum);
                    re = res != null
                        ? new ResponseEnvelope(id, action, true, "Reservation found", res)
                        : new ResponseEnvelope(id, action, false, "Reservation not found", null);
                } catch (Exception e) {
                    logger.error("Error finding reservation #{}: {}", reservationNum, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Approve Reservation":
            case "Reject Reservation":
            case "Cancel Reservation": {
                // ReservationOps.updateReservation to change status,
                // then viewReservation to get updated object for push payload,
                // then pushUpdateToAllClients notifies ALL connected clients 
                int reservationNum = (Integer) data;
                String newStatus   = action.equals("Approve Reservation") ? "APPROVED"
                                   : action.equals("Reject Reservation")  ? "REJECTED"
                                                                           : "CANCELLED";
                ReservationOps resOps = new ReservationOps();
                try {
                    // Update status only - date/time params null so existing values preserved
                    resOps.updateReservation(reservationNum, null, null, null, newStatus);
                    // viewReservation returns the updated Reservation for the push payload
                    Reservation updated = resOps.viewReservation(reservationNum);
                    // PUSH REAL-TIME UPDATE TO ALL CONNECTED CLIENTS
                    if (updated != null) {
                        pushUpdateToAllClients(updated,
                            "Reservation #" + reservationNum + " is now " + newStatus);
                    }
                    logger.info("RESERVATION {}: #{}", newStatus, reservationNum);
                    audit.info("RESERVATION {} | reservationNum={}", newStatus, reservationNum);
                    re = new ResponseEnvelope(id, action, true,
                        "Reservation " + newStatus.toLowerCase() + " successfully", updated);
                } catch (Exception e) {
                    logger.error("Error updating reservation #{} to {}: {}", reservationNum, newStatus, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Delete Reservation": {
                // ReservationOps.deleteReservation (Hibernate)
                int reservationNum = (Integer) data;
                ReservationOps resOps = new ReservationOps();
                try {
                    resOps.deleteReservation(reservationNum);
                    logger.info("DELETE RESERVATION: #{}", reservationNum);
                    audit.info("DELETE RESERVATION | reservationNum={}", reservationNum);
                    re = new ResponseEnvelope(id, action, true, "Reservation deleted", null);
                } catch (Exception e) {
                    logger.error("Error deleting reservation #{}: {}", reservationNum, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            // ==================================================================
            //  LAB - ResourcesManager 
            // ==================================================================

            case "Add Lab": {
                Lab lab = (Lab) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    logger.info("ADD LAB START: {}", lab.getLabId());

                    rm.createLabOnly(lab);

                    logger.info("ADD LAB SUCCESS: {}", lab.getLabId());
                    re = new ResponseEnvelope(id, action, true, "Lab added", null);
                } catch (Exception e) {
                    logger.error("ADD LAB FAILED: {}", lab.getLabId(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }
            case "Add Lab With Equipment": {
                // ResourcesManager.createLabWithEquipment 
                // payload: Object[]{ Lab, List<Equipment> }
                Object[]        payload = (Object[]) data;
                Lab             lab     = (Lab) payload[0];
                List<Equipment> equips  = (List<Equipment>) payload[1];
                ResourcesManager rm     = new ResourcesManager();
                try {
                    rm.createLabWithEquipment(lab, equips);
                    logger.info("ADD LAB WITH EQUIPMENT: {}", lab.getLabId());
                    re = new ResponseEnvelope(id, action, true, "Lab with equipment added", null);
                } catch (Exception e) {
                    logger.error("Error adding lab with equipment {}: {}", lab.getLabId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Find Lab": {
                // ResourcesManager.readLab 
                String labId = (String) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.readLab(labId);
                    logger.info("FIND LAB: {}", labId);
                    re = new ResponseEnvelope(id, action, true, "Lab found", null);
                } catch (Exception e) {
                    logger.error("Error finding lab {}: {}", labId, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Update Lab": {
                // ResourcesManager.updateLab
                Lab lab = (Lab) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.updateLab(lab);
                    logger.info("UPDATE LAB: {}", lab.getLabId());
                    re = new ResponseEnvelope(id, action, true, "Lab updated", null);
                } catch (Exception e) {
                    logger.error("Error updating lab {}: {}", lab.getLabId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Delete Lab": {
                // ResourcesManager.deleteLab 
                Lab lab = (Lab) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.deleteLab(lab);
                    logger.info("DELETE LAB: {}", lab.getLabId());
                    audit.info("DELETE LAB | labId={}", lab.getLabId());
                    re = new ResponseEnvelope(id, action, true, "Lab deleted", null);
                } catch (Exception e) {
                    logger.error("Error deleting lab {}: {}", lab.getLabId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            // ==================================================================
            //  SEATS - methods on Lab.java (JDBC internally via Driver.getConnection)
            //  addSeats, removeSeats, updateSeatStatus, checkSeatStatus
            // ==================================================================

            case "Add Seats": {
                // Lab.addSeats - inserts a seat record for every seat in the lab capacity
                // payload: Lab object
                Lab lab = (Lab) data;
                try {
                    lab.addSeats(lab);
                    logger.info("ADD SEATS: lab={} capacity={}", lab.getLabId(), lab.getSeatCapacity());
                    re = new ResponseEnvelope(id, action, true, "Seats added", null);
                } catch (Exception e) {
                    logger.error("Error adding seats for lab {}: {}", lab.getLabId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Remove Seats": {
                // Lab.removeSeats - deletes  seat records for the lab
                // payload: Lab object
                Lab lab = (Lab) data;
                try {
                    lab.removeSeats(lab);
                    logger.info("REMOVE SEATS: lab={}", lab.getLabId());
                    re = new ResponseEnvelope(id, action, true, "Seats removed", null);
                } catch (Exception e) {
                    logger.error("Error removing seats for lab {}: {}", lab.getLabId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Update Seat Status": {
                // Lab.updateSeatStatus - checks availability then updates status for a single seat
                // payload: Object[]{ seatID (String), newStatus (String) }
                Object[] sp        = (Object[]) data;
                String   seatId    = (String) sp[0];
                String   newStatus = (String) sp[1];
                Lab lab = new Lab();
                try {
                    lab.updateSeatStatus(seatId, newStatus);
                    logger.info("UPDATE SEAT STATUS: {} -> {}", seatId, newStatus);
                    re = new ResponseEnvelope(id, action, true, "Seat status updated", null);
                } catch (Exception e) {
                    logger.error("Error updating seat status {}: {}", seatId, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Check Seat Status": {
                // Lab.checkSeatStatus - returns true if AVAILABLE, false otherwise
                // payload: seatID (String)
                String seatId = (String) data;
                Lab lab = new Lab();
                try {
                    Boolean available = lab.checkSeatStatus(seatId);
                    logger.info("CHECK SEAT STATUS: {} available={}", seatId, available);
                    re = new ResponseEnvelope(id, action, true,
                        available ? "Seat is available" : "Seat is not available", available);
                } catch (Exception e) {
                    logger.error("Error checking seat status {}: {}", seatId, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            // ==================================================================
            //  EQUIPMENT - ResourcesManager 
            // ==================================================================

            case "Add Equipment": {
                // ResourcesManager.createEquipment
                Equipment equip = (Equipment) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.createEquipment(equip);
                    logger.info("ADD EQUIPMENT: {} status={}", equip.getEquipId(), equip.getStatus());
                    re = new ResponseEnvelope(id, action, true, "Equipment added", null);
                } catch (Exception e) {
                    logger.error("Error occurred while adding equipment: {}", equip.getEquipId(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Find Equipment": {
                // ResourcesManager.readEquipment
                String equipId = (String) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.readEquipment(equipId);
                    logger.info("FIND EQUIPMENT: {}", equipId);
                    re = new ResponseEnvelope(id, action, true, "Equipment found", null);
                } catch (Exception e) {
                    logger.error("Error finding equipment {}: {}", equipId, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Update Equipment": {
                // ResourcesManager.updateEquipment
                Equipment equip = (Equipment) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.updateEquipment(equip);
                    logger.info("UPDATE EQUIPMENT: {}", equip.getEquipId());
                    re = new ResponseEnvelope(id, action, true, "Equipment updated", null);
                } catch (Exception e) {
                    logger.error("Error updating equipment {}: {}", equip.getEquipId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Delete Equipment": {
                // ResourcesManager.deleteEquipment 
                Equipment equip = (Equipment) data;
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.deleteEquipment(equip);
                    logger.info("DELETE EQUIPMENT: {}", equip.getEquipId());
                    audit.info("DELETE EQUIPMENT | equipId={}", equip.getEquipId());
                    re = new ResponseEnvelope(id, action, true, "Equipment deleted", null);
                } catch (Exception e) {
                    logger.error("Error deleting equipment {}: {}", equip.getEquipId(), e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Update Equipment Status": {
                // ResourcesManager.readEquipment then updateEquipment 
                // payload: Object[]{ equipId (String), newStatus (EquipStatus), Equipment object }
                Object[]    sp      = (Object[]) data;
                String      equipId = (String) sp[0];
                EquipStatus status  = (EquipStatus) sp[1];
                Equipment   equip   = (Equipment) sp[2];
                ResourcesManager rm = new ResourcesManager();
                try {
                    rm.readEquipment(equipId);   // verify it exists first
                    rm.updateEquipment(equip);   // persist the updated equipment
                    logger.info("UPDATE EQUIP STATUS: {} -> {}", equipId, status.name());
                    re = new ResponseEnvelope(id, action, true, "Status updated to " + status.name(), null);
                } catch (Exception e) {
                    logger.error("Error updating equipment status {}: {}", equipId, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            // ==================================================================
            //  USERS - UserOps 
            // ==================================================================

            case "Find User": {
                // UserOps.findUserById (JDBC)
                String userID = (String) data;
                UserOps userOps = new UserOps();
                try {
                    User found = userOps.findUserById(userID);
                    re = found != null
                        ? new ResponseEnvelope(id, action, true, "User found", found)
                        : new ResponseEnvelope(id, action, false, "User not found", null);
                } catch (Exception e) {
                    logger.error("Error finding user {}: {}", userID, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Update User Role": {
                // UserOps.findUserById then updateUser (JDBC)
                // payload: Object[]{ userID (String), newRole (Roles) }
                Object[] rp      = (Object[]) data;
                String   userID  = (String) rp[0];
                Roles    newRole = (Roles) rp[1];
                UserOps userOps  = new UserOps();
                try {
                    User user = userOps.findUserById(userID);
                    if (user != null) {
                        user.setRole(newRole);
                        userOps.updateUser(user);
                        logger.info("UPDATE ROLE: {} -> {}", userID, newRole.name());
                        audit.info("UPDATE ROLE | userID={} newRole={}", userID, newRole.name());
                        re = new ResponseEnvelope(id, action, true, "Role updated to " + newRole.name(), null);
                    } else {
                        re = new ResponseEnvelope(id, action, false, "User not found", null);
                    }
                } catch (Exception e) {
                    logger.error("Error updating role for {}: {}", userID, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            case "Delete User": {
                // UserOps.deleteUser (JDBC)
                String userID = (String) data;
                UserOps userOps = new UserOps();
                try {
                    int rows = userOps.deleteUser(userID);
                    logger.info("DELETE USER: {}", userID);
                    audit.info("DELETE USER | userID={}", userID);
                    re = new ResponseEnvelope(id, action, rows == 1,
                        rows == 1 ? "User deleted" : "User not found", null);
                } catch (Exception e) {
                    logger.error("Error deleting user {}: {}", userID, e.getMessage(), e);
                    re = new ResponseEnvelope(id, action, false, "Database error: " + e.getMessage(), null);
                }
                break;
            }

            default: {
                logger.warn("UNKNOWN ACTION: {}", action);
                re = new ResponseEnvelope(id, action, false, "Unknown action: " + action, null);
                break;
            }

            } } catch (Exception e) {
                // Catch-all for unexpected dispatch errors
                logger.error("Dispatch error for action {}: {}", action, e.getMessage(), e);
                re = new ResponseEnvelope(id, action, false, "Server error: " + e.getMessage(), null);
            }

            return re;
        }

        // ── Close all streams and socket on disconnect or logout ───────────────
        // Only server-specific method created - all DB operations use
        // ResourcesManager, ReservationOps, UserOps, or Lab instances directly.
        private void closeConnection() {
            try {
                connectedClients.remove(clientId);
                if (objOs != null) objOs.close();
                if (objIs != null) objIs.close();
                if (socket != null && !socket.isClosed()) socket.close();
                logger.info("CONNECTION CLOSED: {}", clientId);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    } // END ClientHandler

} // END Server
