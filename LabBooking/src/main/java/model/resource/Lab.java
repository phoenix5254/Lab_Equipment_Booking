package model.resource;


import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "labs")
public class Lab implements Seats {

     @Serial private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "labId")
    private String labId;
    private String labName;
    private String location;
    private int seatCapacity; // number of seats in the lab
   @OneToMany(mappedBy = "lab", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
private List<SeatRecord> seatDisplay;// all records are adjusted with the lab they belong to

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
        this.seatDisplay = addSeats(this);
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
        this.seatDisplay=addSeats(this);
    }

    public List<SeatRecord> getSeatDisplay() {
        return seatDisplay;
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
    public List<SeatRecord> addSeats(Lab lab) {
        List<SeatRecord> listOfSeats = new ArrayList<>();

        for (int seatNum = 0; seatNum < lab.getSeatCapacity(); seatNum++) {
            SeatRecord newSeat = new SeatRecord(lab, seatNum);
            listOfSeats.add(newSeat);
        }

        SeatRecord.alphaIndex++;
        return listOfSeats;
    }
    

}