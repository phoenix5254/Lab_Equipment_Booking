package controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.resource.Equipment;
import model.resource.EquipmentReserved;
import model.resource.Lab;
import model.resource.Reservation;
import model.resource.SeatRecord;
import model.users.User;

public class ReservationManager {
    private static Connection myConn;
    private static int reservationNum;
    private Reservation rs;
    private EquipmentReserved er;
    
    // Integrated ResourcesManager for Hibernate operations
    private ResourcesManager rm;

    public ReservationManager() {
        UserManager um = new UserManager();
        myConn = UserManager.getConnection();
        
        // Initialize the Hibernate-based resource manager
        rm = new ResourcesManager();
        
        reservationNum = getnextReservationNum();
        rs = new Reservation();
    }

    public ReservationManager(String userId, LocalDate reservationDate, LocalTime startTime,
            LocalTime endTime, String status, String labId, String seatId, List<Equipment> equipmentList,
            int equipQty) {
        rm = new ResourcesManager();
        rs = new Reservation(userId, reservationDate, startTime, endTime, status, labId, seatId, equipmentList,
                equipQty, reservationNum);
        
        // Safety check for empty list before accessing index
        if (!equipmentList.isEmpty()) {
            er = new EquipmentReserved(rs.getReservationNum(), equipmentList.get(0).getEquipId(), equipQty);
        }
    }

    public ReservationManager(Reservation rs) {
        this.rm = new ResourcesManager();
        this.rs = rs;
    }

    // --- Database Logic ---

    static int getnextReservationNum() {
        String sql = "SELECT MAX(reservationNum) FROM reservation";
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Creates a reservation for equipment.
     * Uses Hibernate (rm) to update the Equipment entity's state.
     */
    public int createReserveEquip(int reservationNum, String equipmentID, int equipmentQty) {
        String sql = "INSERT INTO reservationequip(reservationNum, equipId, equipmentQty) VALUES (?, ?, ?)";
        int affectedRows = 0;

        // Use ResourcesManager (Hibernate) to get current equipment state
        Equipment equipment = rm.readEquipment(equipmentID);

        if (equipment != null && equipment.getQtyAvailable() >= equipmentQty) {
            try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
                stmt.setInt(1, reservationNum);
                stmt.setString(2, equipmentID);
                stmt.setInt(3, equipmentQty);

                // Update the quantity using Hibernate through ResourcesManager
                int newQty = equipment.getQtyAvailable() - equipmentQty;
                equipment.setQtyAvailable(newQty);
                rm.updateEquipment(equipment);

                affectedRows = stmt.executeUpdate();
                return affectedRows;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Insertion failed: " + e.getMessage(), "Insert Status",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            int available = (equipment != null) ? equipment.getQtyAvailable() : 0;
            JOptionPane.showMessageDialog(null, "Not enough equipment available. Available quantity: " + available,
                    "Insert Status", JOptionPane.WARNING_MESSAGE);
        }
        return affectedRows;
    }

    /**
     * Logic for deleting a reservation equipment entry.
     * Restores quantity to the Equipment table via Hibernate.
     */
    public int deleteReserveEquip(int reservationNum, String equipmentID) {
        String sql = "DELETE FROM reservationequip WHERE reservationNum = ? AND equipId = ?";
        int affectedRows = 0;
        
        EquipmentReserved equipReserved = readReservationNum(reservationNum, equipmentID);
        
        if (equipReserved != null) {
            try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
                stmt.setInt(1, reservationNum);
                stmt.setString(2, equipmentID);
                
                // Fetch current equipment and restore quantity via Hibernate
                Equipment equipment = rm.readEquipment(equipmentID);
                if (equipment != null) {
                    equipment.setQtyAvailable(equipment.getQtyAvailable() + equipReserved.getEquipmentQty());
                    rm.updateEquipment(equipment);
                }

                affectedRows = stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Deletion failed: " + e.getMessage(), "Delete Status",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return affectedRows;
    }

    public Reservation readReservation(int reservationNum) {
        String sql = "SELECT * FROM reservation WHERE reservationNum = ?";
        Reservation reservation = null;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);

            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    reservation = new Reservation(
                            result.getString("userId"),
                            result.getDate("reservationDate").toLocalDate(),
                            result.getTime("startTime").toLocalTime(),
                            result.getTime("endTime").toLocalTime(),
                            result.getString("status"),
                            result.getString("labId"),
                            result.getString("seatId"),
                            0, 
                            reservationNum);

                    // Pulling linked equipment using ResourcesManager (Hibernate)
                    String sqlEquip = "SELECT equipId FROM reservationequip WHERE reservationNum = ?";
                    try (PreparedStatement stmtEquip = myConn.prepareStatement(sqlEquip)) {
                        stmtEquip.setInt(1, reservationNum);
                        try (ResultSet resultEquip = stmtEquip.executeQuery()) {
                            List<Equipment> equipmentList = new ArrayList<>();

                            while (resultEquip.next()) {
                                String id = resultEquip.getString("equipId");
                                Equipment e = rm.readEquipment(id);
                                if (e != null) equipmentList.add(e);
                            }
                            reservation.setEquipmentList(equipmentList);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservation;
    }

    // Helper method used in delete logic
    public EquipmentReserved readReservationNum(int reservationNum, String equipmentID) {
        String sql = "SELECT * FROM reservationequip WHERE reservationNum = ? AND equipId = ?";
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);
            stmt.setString(2, equipmentID);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return new EquipmentReserved(
                        result.getInt("reservationNum"), 
                        result.getString("equipId"), 
                        result.getInt("equipmentQty")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int createReservation(Reservation reservation) {
    String sql = "INSERT INTO reservation (reservationNum, userId, reservationDate, startTime, endTime, status, labId, seatId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    int affectedRows = 0;
    
    try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
        // ... (existing PreparedStatement mapping for the reservation table)
        stmt.setInt(1, reservation.getReservationNum());
        stmt.setString(2, reservation.getUserId());
        stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
        stmt.setTime(4, Time.valueOf(reservation.getStartTime()));
        stmt.setTime(5, Time.valueOf(reservation.getEndTime()));
        stmt.setString(6, reservation.getStatus());
        stmt.setString(7, reservation.getLabId());
        stmt.setString(8, reservation.getSeatId());

        affectedRows = stmt.executeUpdate();

        if (affectedRows > 0) {
            // --- UTILIZING RESOURCES MANAGER (Hibernate) ---
            
            // 1. Fetch the Lab object from the database using Hibernate
            Lab lab = rm.readLab(reservation.getLabId());
            
            if (lab != null) {
                // 2. Find the specific seat in the lab's list and update it
                for (SeatRecord seat : lab.getSeatDisplay()) {
                    if (seat.getSeatID().equals(reservation.getSeatId())) {
                        seat.setStatus("BOOKED");
                        // If SeatRecord has these fields, update them:
                        // seat.setReservationNum(reservation.getReservationNum());
                        // seat.setStartTime(reservation.getStartTime());
                        break; 
                    }
                }
                // 3. Save the changes back to the database via Hibernate merge
                rm.updateLab(lab);
            }

            // 4. Handle Equipment as before
            for (Equipment equipment : reservation.getEquipmentList()) {
                createReserveEquip(reservation.getReservationNum(), equipment.getEquipId(), reservation.getEquipmentQty());
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Reservation Creation failed: " + e.getMessage());
    }
    return affectedRows;
}

    // --- Boilerplate and Main ---
    
    public static void main(String[] args) {
    // 1. Initialization
    new UserManager(); 
    ResourcesManager resMngRM = new ResourcesManager(); // To fetch equipment
    ReservationManager resMng = new ReservationManager();
    
    // Ensure the ID is unique/valid
    int nextResId = ReservationManager.getnextReservationNum();

    // 2. Prepare 3 Equipment objects from the database (Lab 1: L01)
    // Based on your init code, IDs are E0101, E0102, E0103
    List<Equipment> selectedEquipment = new ArrayList<>();
    String[] idsToBook = {"E0101", "E0102", "E0103"};
    
    for (String id : idsToBook) {
        Equipment e = resMngRM.readEquipment(id);
        if (e != null) {
            selectedEquipment.add(e);
        }
    }

    // 3. Create the Reservation object with the list and quantity per item
    Reservation reservation = new Reservation(
        "123",                          // userId
        LocalDate.now(),                // reservationDate
        LocalTime.now(),                // startTime
        LocalTime.now().plusHours(1),   // endTime
        "PENDING",                      // status
        "L01",                          // labId
        "A1",                           // seatId
        selectedEquipment,              // The List of 3 items
        2,                              // equipmentQty (Deduced 2 from EACH item)
        nextResId                       // Generated Reservation ID
    );

    // 4. Execute the creation logic
    int affectedRows = resMng.createReservation(reservation);

    // 5. Verify the result
    if (affectedRows > 0) {
        System.out.println("Reservation " + nextResId + " created successfully.");
        System.out.println("Seat A1 updated, Equipment quantity reduced by 2 for each item.");
    } else {
        System.out.println("Reservation failed.");
    }
}
}