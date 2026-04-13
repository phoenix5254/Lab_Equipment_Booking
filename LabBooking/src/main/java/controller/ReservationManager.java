package controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import model.resource.Equipment;
import model.resource.EquipmentReserved;
import model.resource.Lab;
import model.resource.Reservation;
import model.resource.SeatRecord;

public class ReservationManager {
    private static Connection myConn;
    private static int reservationNum;
    private ResourcesManager rm;

    public ReservationManager() {
        new UserManager(); // Initialize connection via UserManager
        myConn = UserManager.getConnection();
        rm = new ResourcesManager();
        reservationNum = getnextReservationNum();
    }

    // Get the next available reservation number from database to show persistent ID
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

    public int createReserveEquip(int reservationNum, String equipmentID, int equipmentQty) {
        String sql = "INSERT INTO reservationequip(reservationNum, equipId, equipQty) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);
            stmt.setString(2, equipmentID);
            stmt.setInt(3, equipmentQty);

            Equipment equipment = rm.readEquipment(equipmentID);
            if (equipment != null) {// decrements available quantity in equipment   
                int newQty = equipment.getQtyAvailable() - equipmentQty;
                equipment.setQtyAvailable(newQty);
                rm.updateEquipment(equipment); 
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //update method can be use for decrement or increment
    // zero as the new value means release resource and + values reserve resources
    public int updateReserveEquip(int reservationNum, String equipmentID, int newQty) {
        EquipmentReserved oldRecord = readReservedEquipmentForReservation(reservationNum, equipmentID);
        if (oldRecord == null) {
            System.out.println("No existing reservation found for this equipment.");
            return 0;
        }

        int oldQty = oldRecord.getEquipmentQty();
        int difference = newQty - oldQty;// difference between new quantity and old quantity

        String sql = "UPDATE reservationequip SET equipQty=? WHERE equipId=? AND reservationNum=?";
        int affectedRows = 0;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, newQty);
            stmt.setString(2, equipmentID);
            stmt.setInt(3, reservationNum);
            affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Equipment equipment = rm.readEquipment(equipmentID);
                if (equipment != null) {
                    equipment.setQtyAvailable(equipment.getQtyAvailable() - difference);// decrements available quantity in equipment
                    rm.updateEquipment(equipment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return affectedRows;
    }

    public int deleteReserveEquip(int reservationNum, String equipmentID) {
        String sql = "DELETE FROM reservationequip WHERE reservationNum = ? AND equipId = ?";
        int affectedRows = 0;
        EquipmentReserved equipReserved = readReservedEquipmentForReservation(reservationNum, equipmentID);

        if (equipReserved != null) {
            try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
                stmt.setInt(1, reservationNum);
                stmt.setString(2, equipmentID);

                Equipment equipment = rm.readEquipment(equipmentID);
                if (equipment != null) {// increments available quantity in equipment
                    equipment.setQtyAvailable(equipment.getQtyAvailable() + equipReserved.getEquipmentQty());
                    rm.updateEquipment(equipment);
                }
                affectedRows = stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Deletion failed: " + e.getMessage());
            }
        }
        return affectedRows;
    }
    static public String getReservationEquipmentString(int resNum) {
    StringBuilder sb = new StringBuilder();
    String sql = "SELECT * FROM reservationequip WHERE reservationNum = ?";
    
    sb.append("Details for Reservation ID: ").append(resNum).append("\n");
    sb.append(String.format("%-15s | %-10s%n", "Equipment ID", "Quantity"));
    sb.append("------------------------------------------\n");

    try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
        stmt.setInt(1, resNum);
        
        try (ResultSet result = stmt.executeQuery()) {
            int count = 0;
            while (result.next()) {
                String eId = result.getString("equipId");
                int qty = result.getInt("equipQty");

                sb.append(String.format("%-15s | %-10d%n", eId, qty));
                count++;
            }

            if (count == 0) {
                return "No equipment found for Reservation #" + resNum;
            }

            sb.append("------------------------------------------\n");
            sb.append("Total Items: ").append(count);
        }
    } catch (SQLException e) {
        sb.append("Database Error: ").append(e.getMessage());
        e.printStackTrace();
    }

    return sb.toString();
}
    public Reservation readReservation(int reservationNum) {
        String sql = "SELECT * FROM reservation WHERE reservationNum = ?";
        Reservation reservation = null;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    // Note: We leave the quantity array empty initially and fill it via the join
                    // table
                    reservation = new Reservation(
                            result.getString("userId"),
                            result.getDate("reservationDate").toLocalDate(),
                            result.getTime("startTime").toLocalTime(),
                            result.getTime("endTime").toLocalTime(),
                            result.getString("status"),
                            result.getString("labId"),
                            result.getString("seatId"),
                            null, // Quantity array to be populated if needed
                            reservationNum);

                    String sqlEquip = "SELECT equipId FROM reservationequip WHERE reservationNum = ?";
                    try (PreparedStatement stmtEquip = myConn.prepareStatement(sqlEquip)) {
                        stmtEquip.setInt(1, reservationNum);
                        try (ResultSet resultEquip = stmtEquip.executeQuery()) {
                            List<Equipment> equipmentList = new ArrayList<>();
                            while (resultEquip.next()) {
                                Equipment e = rm.readEquipment(resultEquip.getString("equipId"));
                                if (e != null)
                                    equipmentList.add(e);
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

    public EquipmentReserved readReservedEquipmentForReservation(int reservationNum, String equipmentID) {
        String sql = "SELECT * FROM reservationequip WHERE reservationNum = ? AND equipId = ?";
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);
            stmt.setString(2, equipmentID);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return new EquipmentReserved(
                            result.getInt("reservationNum"),
                            result.getString("equipId"),
                            result.getInt("equipQty"));
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
            // ... (Binding parameters 1 through 8 as before) ...
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
                // 1. Update Seat Status via Hibernate
                Lab lab = rm.readLab(reservation.getLabId());
                if (lab != null) {
                    for (SeatRecord seat : lab.getSeatDisplay()) {
                        if (seat.getSeatID().equals(reservation.getSeatId())) {
                            seat.setStatus("BOOKED");

                            // Corrected SQL: Use AND to join conditions in a WHERE clause
                            String sq = "UPDATE seatsrecords SET reservationNum=? WHERE seatID=? AND labId=?";
                            try (PreparedStatement stm = myConn.prepareStatement(sq)) {
                                stm.setInt(1, reservation.getReservationNum());
                                stm.setString(2, reservation.getSeatId());
                                stm.setString(3, reservation.getLabId());
                                stm.executeUpdate();
                            }
                            break;
                        }
                    }
                    rm.updateLab(lab); // Sync remaining changes via Hibernate
                    List<Equipment> equipList = reservation.getEquipmentList();
                    int[] qtys = reservation.getEquipmentQty();

                    // CRITICAL CHECK: Ensure list and array match
                    if (equipList != null && qtys != null) {
                        if (equipList.size() != qtys.length) {
                            System.err.println("CRITICAL ERROR: Equipment List size (" + equipList.size() +
                                    ") does not match Quantity Array size (" + qtys.length + ")");
                            return 0;
                        }

                        for (int i = 0; i < equipList.size(); i++) {
                            // This maps List[i] to int[i]
                            createReserveEquip(
                                    reservation.getReservationNum(),
                                    equipList.get(i).getEquipId(),
                                    qtys[i]);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error Code: " + e.getErrorCode());
            e.printStackTrace(); // This will tell you if it's a Duplicate Key or Foreign Key error
        }
        return affectedRows;
    }

    public int updateReservationStatus(int resNum, String status) {
        String sql = "UPDATE reservation SET status = ? WHERE reservationNum = ?";
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, resNum);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteReservation(int resNum) {
    int affectedRows = 0;
    
    try {
        // 1. Restore Equipment Quantities & Delete from reservationequip
        // We fetch all equipment associated with this reservation first
        String fetchEquipSql = "SELECT equipId, equipQty FROM reservationequip WHERE reservationNum = ?";
        try (PreparedStatement fetchStmt = myConn.prepareStatement(fetchEquipSql)) {
            fetchStmt.setInt(1, resNum);
            ResultSet rs = fetchStmt.executeQuery();
            
            while (rs.next()) {
                String eId = rs.getString("equipId");
                int qtyReserved = rs.getInt("equipQty");
                
                // Use Hibernate (rm) to increase the inventory back
                Equipment equipment = rm.readEquipment(eId);
                if (equipment != null) {
                    equipment.setQtyAvailable(equipment.getQtyAvailable() + qtyReserved);
                    rm.updateEquipment(equipment);
                }
            }
        }

        // Now truncate (delete) the links in the mapping table
        String deleteMappingSql = "DELETE FROM reservationequip WHERE reservationNum = ?";
        try (PreparedStatement delMapStmt = myConn.prepareStatement(deleteMappingSql)) {
            delMapStmt.setInt(1, resNum);
            delMapStmt.executeUpdate();
        }

        // 2. Change Seat back to AVAILABLE
        // First, we need to know WHICH seat and lab this reservation was using
        Reservation resRecord = readReservation(resNum);
        if (resRecord != null) {
            String updateSeatSql = "UPDATE seatsrecords SET status = 'AVAILABLE', reservationNum = NULL " +
                                   "WHERE seatID = ? AND labId = ?";
            try (PreparedStatement seatStmt = myConn.prepareStatement(updateSeatSql)) {
                seatStmt.setString(1, resRecord.getSeatId());
                seatStmt.setString(2, resRecord.getLabId());
                seatStmt.executeUpdate();
            }
            
            // Sync the Lab object via Hibernate if necessary
            Lab lab = rm.readLab(resRecord.getLabId());
            if (lab != null) {
                for (SeatRecord s : lab.getSeatDisplay()) {
                    if (s.getSeatID().equals(resRecord.getSeatId())) {
                        s.setStatus("AVAILABLE");
                        break;
                    }
                }
                rm.updateLab(lab);
            }
        }

        // 3. Delete the main Reservation record
        String deleteResSql = "DELETE FROM reservation WHERE reservationNum = ?";
        try (PreparedStatement mainDelStmt = myConn.prepareStatement(deleteResSql)) {
            mainDelStmt.setInt(1, resNum);
            affectedRows = mainDelStmt.executeUpdate();
        }

        // If autocommit is false, commit the whole transaction
        // myConn.commit();
        System.out.println("Reservation " + resNum + " deleted and resources restored.");

    } catch (SQLException e) {
        System.err.println("Error during reservation deletion: " + e.getMessage());
        // myConn.rollback(); 
    }
    
    return affectedRows;
}
    public static void main(String[] args) {
        // 1. Initialize managers
        ReservationManager resMng = new ReservationManager();
        ResourcesManager resRM = new ResourcesManager();

       /*/ // 2. Fetch specific equipment from the database
        List<Equipment> selectedEquipment = new ArrayList<>();
        String[] idsToBook = { "E0101", "E0102", "E0103" };

        for (String id : idsToBook) {
            Equipment e = resRM.readEquipment(id);
            if (e != null) {
                selectedEquipment.add(e);
            }
        }

        // 3. Define parallel quantities (e.g., 1 of E0101, 2 of E0102, 5 of E0103)
        int[] quantities = { 1, 2, 5 };

        // 4. Create the Reservation object
        // We use the next ID from the database
        int nextId = ReservationManager.getnextReservationNum();

        Reservation newRes = new Reservation(
                "123", // userId
                LocalDate.now(), // date
                LocalTime.of(10, 0), // startTime (10:00 AM)
                LocalTime.of(12, 0), // endTime (12:00 PM)
                "PENDING", // status
                "L01", // labId
                "A1", // seatId
                selectedEquipment, // List<Equipment>
                quantities, // int[]
                nextId // resNum
        );

        // 5. Execute the creation logic
        int result = resMng.createReservation(newRes);

        if (result > 0) {
            System.out.println("Reservation #" + nextId + " created successfully!");
            // Verify by reading it back
            System.out.println(resMng.readReservation(nextId));
        } else {
            System.out.println("Failed to create reservation.");
        }
    }*/
   //System.out.println(resMng.deleteReservation(1));
}
}
