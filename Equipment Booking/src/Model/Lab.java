package Model;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Lab implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String labId;
    private String labName;
    private String location;
    private int seatCapacity;
   // private EquipStatus status;
    private String seatDisplay[][]; // seatID and status
    private ArrayList<Equipment> equipmentList;

    public Lab() {
        labId = "";
        labName = "";
        location = "";
        seatCapacity = 0;
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

    public String[][] getSeatDisplay() {
        return seatDisplay;
    }
    public String printSeatDisplayList() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<seatDisplay.length;i++){
            for(int j=0;j<seatDisplay[i].length;j++){
                sb.append(seatDisplay[i][j]).append(" ");
                
            }
           if((i+1)%2 == 0) sb.append("\n");
        }
        return sb.toString();
    }

    public void setSeatDisplay(String[][] seatDisplay) {
        this.seatDisplay = seatDisplay;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(ArrayList<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }
    public String mainToString() {
        return "Lab ID:" + labId + "\nLab Name:" + labName + "\nLocation:" + location + "\nSeat Capacity:" + seatCapacity;
    }
    public String specificToString(){
        return "Lab ID:" + labId + "\nLab Name:" + labName + "\nLocation:" + location + "\nSeat Capacity:" + seatCapacity+"\n"+"Seaing Placement\n"+printSeatDisplayList();
    }
   

}