package model.resource;

import java.io.Serializable;
import java.util.ArrayList;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="labs")
public class Lab implements Serializable {

   // @Serial  private static final long serialVersionUID = 1L;
    @Id
    @Column(name="labId")
    private String labId;
    private String labName;
    private String location;
    private int seatCapacity; // number of seats in the lab
    @OneToMany
    private ArrayList<SeatRecord> seatDisplay; // seatID and status
    @OneToMany
    private ArrayList<Equipment> equipmentList;

    public Lab() {
        labId = "";
        labName = "";
        location = "";
        seatCapacity = (int)0;
        seatDisplay = new ArrayList<SeatRecord>();
        equipmentList = new ArrayList<Equipment>();
    }

    public Lab(String labId, String labName,String location, int seatCapacity) {
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

    public ArrayList<SeatRecord> getSeatDisplay() {
        return seatDisplay;
    }
    

    /*public void setSeatDisplay(SeatRecord seatDisplay2) {
        this.seatDisplay = seatDisplay2;
    }*/

    public ArrayList<Equipment> getEquipmentList() {
        return equipmentList;
    }

    /*public void setEquipmentList(Equipment equipmentList) {
        this.equipmentList = equipmentList
    }*/
    public String mainToString() {
        return "Lab ID:" + labId + "\nLab Name:" + labName + "\nLocation:" + location + "\nSeat Capacity:" + seatCapacity;
    }
   

}