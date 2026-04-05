package model.resource;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import model.users.Enums.EquipStatus;

@Entity
@Table(name = "SeatsRecords")
public class SeatRecord implements Serializable {
    @Id
    @Column(name = "seatID")
    private String seatID;
    @ManyToOne
    @JoinColumn(name = "lab") // This tells Hibernate to use your actual column name
    private Lab lab;
    private String status;
  
    @Transient // This tells Hibernate: "Do NOT look for a column for this"
    String alpha[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z" };
    @Transient // Ignore this too
    static int alphaIndex = 0;
 

    public SeatRecord() {
        this.seatID = "";
        this.status = EquipStatus.UNKNOWN.toString();
        this.lab = new Lab();
    }

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
