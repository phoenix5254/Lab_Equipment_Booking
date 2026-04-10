package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import controller.UserOps;//
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "labs")
public class Lab implements Seats {

    // @Serial private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "labId")
    private String labId;
    private String labName;
    private String location;
    private int seatCapacity; // number of seats in the lab
    @OneToMany(mappedBy = "lab", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatRecord> seatDisplay = new ArrayList<>();// all records are adjusted with the lab they belong to

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lab") // one lab has many equipment items
    private List<Equipment> equipmentList = new ArrayList<>();

    public Lab() {
        labId = "";
        labName = "";
        location = "";
        seatCapacity = (int) 0;
        seatDisplay = new ArrayList<SeatRecord>();
        equipmentList = new ArrayList<Equipment>();
    }

    public Lab(String labId, String labName, String location, int seatCapacity) {
        this.labId = labId;
        this.labName = labName;
        this.location = location;
        this.seatCapacity = seatCapacity;

    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public List<SeatRecord> getSeatDisplay() {
        return  seatDisplay;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public String mainToString() {
        return "Lab ID:" + labId + "\nLab Name:" + labName + "\nLocation:" + location + "\nSeat Capacity:"
                + seatCapacity;
    }

    @Override
    public void addSeats(Lab lab) {
        Connection myConn = UserOps.getConnection();
        {
            for (int seatNum = 0; seatNum < lab.getSeatCapacity(); seatNum++) {
                SeatRecord newSeat = new SeatRecord(lab, seatNum);
                String sql = "INSERT INTO seatsRecords (seatID, status, lab) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
                    pstmt.setString(1, newSeat.getSeatID());
                    pstmt.setString(2, newSeat.getStatus());
                    pstmt.setString(3, newSeat.getLabName().getLabId());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Insertion failed: " + e.getMessage(), "Insert Status",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        SeatRecord.alphaIndex++;
    }

    public void removeSeats(Lab lab) {
        Connection myConn = UserOps.getConnection();
        String sql = "DELETE FROM seatsRecords WHERE lab = ?";
        try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
            pstmt.setString(1, lab.getLabId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Deletion failed: " + e.getMessage(), "Delete Status",
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Failed to remove seats", e);
        }
    }

    public void updateSeatStatus(String seatID, String newStatus) {
        boolean status = checkSeatStatus(seatID);
        if (!status) {
            JOptionPane.showMessageDialog(null, "Seat is not available: " + seatID, "Seat Status",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            Connection myConn = UserOps.getConnection();
            String sql = "UPDATE seatsRecords SET status = ? WHERE seatID = ?";
            try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, seatID);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Update failed: " + e.getMessage(), "Update Status",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public Boolean checkSeatStatus(String seatID) {
        Connection myConn = UserOps.getConnection();
        String sql = "SELECT status FROM seatsRecords WHERE seatID = ?";
        try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
            pstmt.setString(1, seatID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return status.equals(EquipStatus.AVAILABLE.toString());
            } else {
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Query failed: " + e.getMessage(), "Query Status",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

}