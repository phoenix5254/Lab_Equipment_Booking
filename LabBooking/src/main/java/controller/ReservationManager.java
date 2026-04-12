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
        String sql = "INSERT INTO reservationequip(reservationNum, equipId, equipQty) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);
            stmt.setString(2, equipmentID);
            stmt.setInt(3, equipmentQty);

            // Fetch current equipment and update quantity via Hibernate
            Equipment equipment = rm.readEquipment(equipmentID);
            int newQty = equipment.getQtyAvailable() - equipmentQty;
            equipment.setQtyAvailable(newQty);
            rm.updateEquipment(equipment); // This updates the DB table
            int result = stmt.executeUpdate();
            // If your database doesn't auto-commit, you need this:
            // myConn.commit();

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
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
                            result.getInt("equipmentQty"));
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
                }

                // 2. Handle Equipment
                List<Equipment> equipList = reservation.getEquipmentList();
                if (equipList != null && !equipList.isEmpty()) {
                    for (Equipment equipment : equipList) {
                        createReserveEquip(
                                reservation.getReservationNum(),
                                equipment.getEquipId(),
                                reservation.getEquipmentQty());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Integrity Error: Check if " + reservation.getLabId() + " exists in labs table.");
            e.printStackTrace();
        }
        return affectedRows;
    }

    public static void main(String[] args) {
        new UserManager();
        ResourcesManager resRM = new ResourcesManager();
        ReservationManager resMng = new ReservationManager();

        // 1. Get the REAL next ID from the DB
        int nextResId = ReservationManager.getnextReservationNum();

        // 2. Fetch the 3 equipment items from the database
        List<Equipment> selectedEquipment = new ArrayList<>();
        String[] idsToBook = { "E0101", "E0102", "E0103" }; // Ensure these match your init data

        for (String id : idsToBook) {
            Equipment e = resRM.readEquipment(id);
            if (e != null) {
                selectedEquipment.add(e);
            }
        }

        // 3. Create the Reservation object with the populated list
        Reservation reservation = new Reservation(
                "123", LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1),
                "PENDING", "L01", "A1",
                selectedEquipment, // PASS THE LIST HERE
                2, // Quantity per item
                nextResId // Use the dynamic ID
        );

        // 4. Run the creation
        resMng.createReservation(reservation);
    }
}