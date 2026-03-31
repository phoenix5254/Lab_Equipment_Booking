package model.resource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import model.users.Enums.EquipStatus;

@Entity
@Table(name="SeatsRecords")
public class SeatRecord {
    @Id
    @Column(name="seatID")
    private String seatID;
    @ManyToOne
    private Lab lab;
    private String status;
    String alpha[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z" };
    static int alphaIndex = 0; // index of lab

    public SeatRecord(Lab lab, int seatNumber) {
        this.seatID = alpha[alphaIndex] + Integer.toString(seatNumber);
        this.status = EquipStatus.AVAILABLE.toString();
        this.lab = lab;
    }

    public Lab getLabName() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public String getSeatID() {
        return seatID;
    }

    public void setSeatID(String seatID) {
        this.seatID = seatID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
