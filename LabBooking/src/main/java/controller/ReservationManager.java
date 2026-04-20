package controller;

import java.io.Serializable;
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
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import model.Enums.EquipStatus;
import model.resource.Equipment;
import model.resource.EquipmentReserved;
import model.resource.Lab;
import model.resource.Reservation;
import model.resource.SeatRecord;
import model.users.User;

public class ReservationManager implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Connection myConn;
    private static int reservationNum;
    private Reservation rs;
    private EquipmentReserved er;

    // Integrated ResourcesManager for Hibernate operations
    private ResourcesManager rm;

    public ReservationManager() {
//        UserManager um = new UserManager();
        myConn = getDatabaseConnection();
        
        // Initialize the Hibernate-based resource manager
        rm = new ResourcesManager();

        reservationNum = getnextReservationNum();
        rs = new Reservation();
    }
    
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
    
    public ReservationManager(String userId, LocalDate reservationDate, LocalTime startTime,
            LocalTime endTime, String status, String labId, String seatId, List<Equipment> equipmentList,
            int equipQty) {
        rm = new ResourcesManager();
        rs = new Reservation(userId, reservationDate, startTime, endTime, labId, seatId, equipmentList,
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

    public static int getnextReservationNum() {
    	myConn = getDatabaseConnection();
        String sql = "SELECT MAX(reservationNum) FROM reservations";
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
    
    public static List<Lab> getAllLabs() {
    	myConn = getDatabaseConnection();
        List<Lab> labs = new ArrayList<>();

        String sql = "SELECT * FROM labs";

        try (PreparedStatement stmt = myConn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while(rs.next()) {

                Lab lab = new Lab();

                lab.setLabId(rs.getString("labId"));
                lab.setLabName(rs.getString("labName"));
                lab.setLocation(rs.getString("location"));
                lab.setSeatCapacity(rs.getInt("seatCapacity"));
                System.out.println("NEW LAB");
                System.out.println(lab.getLabId());
                labs.add(lab);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return labs;
    }
    
    public static List<Equipment> getEquipmentByLabId(String labId) {
    	myConn = getDatabaseConnection();
        List<Equipment> equipmentList = new ArrayList<>();
        
        //REWRITE THE SQL 
        String sql =
            "SELECT * FROM equipment WHERE labId = ?";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setString(1, labId);

            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {

                Equipment eq = new Equipment();

                eq.setEquipId(rs.getString("equipId"));
                eq.setEquipName(rs.getString("equipName"));
                eq.setQtyAvailable(rs.getInt("qtyAvailable"));
                eq.setStatus(rs.getString("status"));

                equipmentList.add(eq);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return equipmentList;
    }
    
    public static void loadAllLabsWithEquipment() {
    	myConn = getDatabaseConnection();
        List<Lab> labs = getAllLabs();

        for(Lab lab : labs) {

            String labId = lab.getLabId();

            List<Equipment> equipmentList = getEquipmentByLabId(labId);

            System.out.println("Lab: " + lab.getLabName());

            for(Equipment e : equipmentList) {

                System.out.println("Equipment: " + e.getEquipName());
            }
        }
    }

    /**
     * Creates a reservation for equipment.
     * Uses Hibernate (rm) to update the Equipment entity's state.
     */
    public int createReserveEquip(int reservationNum, LocalTime startTime, String equipmentID, int equipmentQty) {
        String sql = "INSERT INTO reservation_equipment(reservationNum, startTime, equipmentID) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservationNum);
            Time newStartTime = Time.valueOf(startTime);
            stmt.setTime(2, newStartTime);
            stmt.setString(3, equipmentID);

            // Fetch current equipment and update quantity via Hibernate
            Equipment equipment = ResourcesManager.readEquipment(equipmentID);
            int newQty = equipment.getQtyAvailable() - equipmentQty;
            equipment.setQtyAvailable(newQty);
            if (equipment.getQtyAvailable() == 0) {
            	equipment.setStatus(EquipStatus.UNAVAILABLE.toString());}
            
            System.out.println("Equipment Object Status is now: " + equipment.getStatus());
            ResourcesManager.updateEquipment(equipment); // This updates the DB table
            int result = stmt.executeUpdate();

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
 public int updateReserveEquip(int reservationNum, String equipmentID, int newQty) {
    // 1. Find the existing record to get the OLD quantity
    EquipmentReserved oldRecord = readReservedEquipmentForReservation(reservationNum, equipmentID);
    if (oldRecord == null) {
        System.out.println("No existing reservation found for this equipment.");
        return 0; 
    }

    int oldQty = oldRecord.getEquipmentQty();
    int difference = newQty - oldQty; // Positive means they want more, negative means they are returning some

    // 2. Update the mapping table (JDBC)
    String sql = "UPDATE reservation_equipment SET equipQty=? WHERE equipId=? AND reservationNum=?";
    int affectedRows = 0;

    try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
        stmt.setInt(1, newQty);
        stmt.setString(2, equipmentID);
        stmt.setInt(3, reservationNum);
        affectedRows = stmt.executeUpdate();

        if (affectedRows > 0) {
            // 3. Sync the Inventory (Hibernate)
            Equipment equipment = ResourcesManager.readEquipment(equipmentID);
            if (equipment != null) {
                // Subtract the difference from availability
                // (e.g., if newQty is 5 and oldQty was 2, difference is 3. We subtract 3)
                int updatedAvailability = equipment.getQtyAvailable() - difference;
                equipment.setQtyAvailable(updatedAvailability);
                
                ResourcesManager.updateEquipment(equipment); 
                System.out.println("Inventory synced successfully.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return affectedRows;
}
    /**
     * Logic for deleting a reservation equipment entry.
     * Restores quantity to the Equipment table via Hibernate.
     */
    public int deleteReserveEquip(int reservationNum, String equipmentID) {
        String sql = "DELETE FROM reservation_equipment WHERE reservationNum = ? AND equipId = ?";
        int affectedRows = 0;

        EquipmentReserved equipReserved = readReservedEquipmentForReservation(reservationNum, equipmentID);

        if (equipReserved != null) {
            try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
                stmt.setInt(1, reservationNum);
                stmt.setString(2, equipmentID);

                // Fetch current equipment and restore quantity via Hibernate
                Equipment equipment = ResourcesManager.readEquipment(equipmentID);
                if (equipment != null) {
                    equipment.setQtyAvailable(equipment.getQtyAvailable() + equipReserved.getEquipmentQty());
                    ResourcesManager.updateEquipment(equipment);
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
        String sql = "SELECT * FROM reservations WHERE reservationNum = ?";
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
                    String sqlEquip = "SELECT equipId FROM reservation_equipment WHERE reservationNum = ?";
                    try (PreparedStatement stmtEquip = myConn.prepareStatement(sqlEquip)) {
                        stmtEquip.setInt(1, reservationNum);
                        try (ResultSet resultEquip = stmtEquip.executeQuery()) {
                            List<Equipment> equipmentList = new ArrayList<>();

                            while (resultEquip.next()) {
                                String id = resultEquip.getString("equipId");
                                Equipment e = ResourcesManager.readEquipment(id);
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
    public EquipmentReserved readReservedEquipmentForReservation(int reservationNum, String equipmentID) {
        String sql = "SELECT * FROM reservation_equipment WHERE reservationNum = ? AND equipId = ?";
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
    
    //UPDATED LIZ FOR STUDENTVIEW
    public int createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (reservationNum, userId, reservationDate, startTime, endTime, status, labId, modifiedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int affectedRows = 0;

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setInt(1, reservation.getReservationNum());
            stmt.setString(2, reservation.getUserId());
            stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            stmt.setTime(4, Time.valueOf(reservation.getStartTime()));
            stmt.setTime(5, Time.valueOf(reservation.getEndTime()));
            stmt.setString(6, reservation.getStatus());
            stmt.setString(7, reservation.getLabId());
            stmt.setString(8, reservation.getModifiedAt());
            System.out.println(reservation.getReservationNum());
            System.out.println(reservation.getReservationNum());
            System.out.println(reservation.getUserId());
            System.out.println(Date.valueOf(reservation.getReservationDate()));
            System.out.println(Time.valueOf(reservation.getStartTime()));
            System.out.println(Time.valueOf(reservation.getEndTime()));
            System.out.println(reservation.getStatus());
            System.out.println(reservation.getLabId());
            System.out.println(reservation.getSeatId());
            System.out.println(reservation.getModifiedAt());

            affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // 1. Update Seat Status via Hibernate
                Lab lab = ResourcesManager.readLab(reservation.getLabId());
                if (lab != null) {
                    for (SeatRecord seat : lab.getSeatDisplay()) {
                        if (seat.getSeatID().equals(reservation.getSeatId())) {
                            seat.setStatus("BOOKED");

                            // Corrected SQL: Use AND to join conditions in a WHERE clause
                            String sq = "UPDATE seatsrecords SET reservationNum=?, startTime=? WHERE seatID=? AND labId=?";
                            try (PreparedStatement stm = myConn.prepareStatement(sq)) {
                                stm.setInt(1, reservation.getReservationNum());
                                stm.setTime(2, Time.valueOf(reservation.getStartTime()));
                                stm.setString(3, reservation.getSeatId());
                                stm.setString(4, reservation.getLabId());
                                stm.executeUpdate();
                            }catch (SQLException e){
                            	JOptionPane.showMessageDialog(null, "Reservation Manager: Error updating seatsrecords\n" + e.getMessage());
                            	return 0;
                            }
                            break;
                        }
                    }
                    ResourcesManager.updateLab(lab); // Sync remaining changes via Hibernate
                } else {
                	JOptionPane.showMessageDialog(null, "Lab is null\n");
                }

                // 2. Handle Equipment
                System.out.println("Reservation Manager: Create Reservation - Handling Equipment List");
                List<Equipment> equipList = reservation.getEquipmentList();
                if (equipList != null && !equipList.isEmpty()) {
                    for (Equipment equipment : equipList) {
                        createReserveEquip(
                                reservation.getReservationNum(),
                                reservation.getStartTime(),
                                equipment.getEquipId(),
                                reservation.getEquipmentQty());
                    }
                }else {
                	JOptionPane.showMessageDialog(null, "Reservation Manager: Equip List is NULL or Empty");
                }
            }
        } catch (SQLException e) {
            System.err.println("Integrity Error: Check if " + reservation.getLabId() + " exists in labs table.");
            e.printStackTrace();
        }
        return affectedRows;
    }
    
    
    /*// Every thing about reservation except resNumber UserId and status for Students
    public int updateReservation(Reservation reservation) {
        String sql = "UPDATE reservation SET reservationDate = ?, startTime = ?, endTime = ?, labId = ?, seatId = ? WHERE reservationNum = ? AND userId = ? AND status = ?";
        int affectedRows = 0;
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(reservation.getReservationDate()));
            stmt.setTime(2, Time.valueOf(reservation.getStartTime()));
            stmt.setTime(3, Time.valueOf(reservation.getEndTime()));
            stmt.setString(4, reservation.getLabId());
            stmt.setString(5, reservation.getSeatId());
            stmt.setInt(6, reservation.getReservationNum());
            stmt.setString(7, reservation.getUserId());
            stmt.setString(8, reservation.getStatus());
            
            affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage(), "Update Status",
                    JOptionPane.ERROR_MESSAGE);
        }
        return affectedRows;
    }*/

    // update status of reservation
    public int updateReservation(int reservation, String status) {
        String sql = "UPDATE reservation SET status = ? WHERE reservationNum = ?";
        int affectedRows = 0;
        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, reservation);
            affectedRows = stmt.executeUpdate();
            System.out.println("Updated status for reservation " + reservation + " to " + status);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage(), "Update Status",
                    JOptionPane.ERROR_MESSAGE);
        }
        return affectedRows;
    }
    
    public static List<String> getAvailableSeatsByLab(String labId) {
    	myConn = getDatabaseConnection();
        List<String> seats = new ArrayList<>();

        String sql = "SELECT seatID FROM seatsrecords WHERE labId = ? AND status = 'AVAILABLE'";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setString(1, labId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seats.add(rs.getString("seatID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seats;
    }
    
    
    
    public static List<Reservation> getReservationsByLabId(String labId) {
    	    myConn = getDatabaseConnection();
    	    List<Reservation> reservations = new ArrayList<>();

    	    String sql =
    	        "SELECT r.*, s.seatID " +
    	        "FROM reservations r " +
    	        "LEFT JOIN seatsrecords s ON r.reservationNum = s.reservationNum " +
    	        "WHERE r.labId = ?";

    	    try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

    	        stmt.setString(1, labId);

    	        ResultSet rs = stmt.executeQuery();

    	        while (rs.next()) {

    	            Reservation reservation = new Reservation();

    	            reservation.setReservationNum(rs.getInt("reservationNum"));
    	            reservation.setUserId(rs.getString("userId"));
    	            reservation.setLabId(rs.getString("labId"));
    	            reservation.setReservationDate(rs.getDate("reservationDate").toLocalDate());
    	            reservation.setStartTime(rs.getTime("startTime").toLocalTime());
    	            reservation.setEndTime(rs.getTime("endTime").toLocalTime());
    	            reservation.setStatus(rs.getString("status"));

    	            String seat = rs.getString("seatID");
    	            System.out.println("Seat fetched = " + seat);

    	            reservation.setSeat(seat);

    	            reservations.add(reservation);
    	        }

    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }

    	    return reservations;
    	}
        
  //helper for getreservation method to approve reservation to get equipment associated with reservation
    private List<Equipment> getEquipmentForReservation(int reservationNum) {

        List<Equipment> list = new ArrayList<>();

        String sql = "SELECT e.* FROM equipment e " +
                     "JOIN reservation_equipment re ON e.equipId = re.equipmentID " +
                     "WHERE re.reservationNum = ?";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setInt(1, reservationNum);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Equipment eq = new Equipment();
                eq.setEquipId(rs.getString("equipId"));
                eq.setEquipName(rs.getString("equipName"));
                eq.setQtyAvailable(rs.getInt("qtyAvailable"));

                list.add(eq);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
  //helper for getreservation method to approve reservation to get seats associated with e=reservation
    private String getSeatsForReservation(int reservationNum) {

        String sql = "SELECT seatId FROM seatsrecords WHERE reservationNum = ?";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setInt(1, reservationNum);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("seatId");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public List<Reservation> getReservations(String column, String value) {

        List<Reservation> reservationList = new ArrayList<>();

        List<String> validColumns = Arrays.asList(
            "reservationNum", "userId", "labId",
            "reservationDate", "startTime", "endTime", "status" ,"modifiedAt"
        );

        if (!validColumns.contains(column)) {
            throw new IllegalArgumentException("Invalid column selected");
        }

        String sql = "SELECT * FROM reservations WHERE " + column + " LIKE ?";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {

            stmt.setString(1, "%" + value + "%"); // allows partial search

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reservation res = new Reservation();
                int resNum = rs.getInt("reservationNum");

                res.setReservationNum(resNum);
                res.setUserId(rs.getString("userId"));
                res.setLabId(rs.getString("labId"));
                res.setReservationDate(rs.getDate("reservationDate").toLocalDate());
                res.setStartTime(rs.getTime("startTime").toLocalTime());
                res.setEndTime(rs.getTime("endTime").toLocalTime());
                res.setStatus(rs.getString("status"));
                res.setModifiedAt(rs.getString("modifiedAt"));

                
                List<Equipment> equipmentList = getEquipmentForReservation(resNum);
                res.setEquipmentList(equipmentList);

                String seatId = getSeatsForReservation(resNum);
                res.setSeat(seatId);

                reservationList.add(res);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reservationList;
    }
    
    public List<Reservation> getReservationsByUserId(String userId) {
        // Call the existing generic method which already handles 
        // joining equipment and seats for each reservation.
        return getReservations("userId", userId);
    }
    
  //added to help with the save functionality of the approve or reject reservation
    public void updateReservationStatus(int reservationNum, String status) {

        String query = "UPDATE reservations "
                + "SET status = ?, modifiedAt = NOW() "
                + "WHERE reservationNum = ?";
;

        try (PreparedStatement stmt = myConn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, reservationNum);

            stmt.executeUpdate();

            System.out.println("SERVER: Database updated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isEquipmentAlreadyBooked(String labId, String equipmentId,
            LocalDate date, LocalTime startTime, LocalTime endTime) {

        myConn = UserManager.getDatabaseConnection();

        String sql =
            "SELECT COUNT(*) " +
            "FROM reservations r " +
            "JOIN reservation_equipment re ON r.reservationNum = re.reservationNum " +
            "WHERE r.labId = ? " +
            "AND r.reservationDate = ? " +
            "AND re.equipmentID = ? " +
            "AND r.status <> 'CANCELLED' " +
            "AND r.status <> 'REJECTED' " +
            "AND r.startTime < ? " +
            "AND r.endTime > ?";

        try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
            stmt.setString(1, labId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setString(3, equipmentId);
            stmt.setTime(4, java.sql.Time.valueOf(endTime));
            stmt.setTime(5, java.sql.Time.valueOf(startTime));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    //method to check all selected equipment by the student
    public static boolean hasEquipmentConflict(String labId, List<Equipment> equipmentList,
            LocalDate date, LocalTime startTime, LocalTime endTime) {

        if (equipmentList == null || equipmentList.isEmpty()) {
            return false;
        }

        for (Equipment eq : equipmentList) {
            if (isEquipmentAlreadyBooked(labId, eq.getEquipId(), date, startTime, endTime)) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
//        ResourcesManager resRM = new ResourcesManager();
        ReservationManager resMng = new ReservationManager();
        ReservationManager.getAllLabs();

       /* // 1. Get the REAL next ID from the DB
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
        resMng.createReservation(reservation);*/
//        System.out.println(resMng.readReservation(1));
    }
}
