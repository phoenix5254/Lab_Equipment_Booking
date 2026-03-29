package com_java_users;
import java.util.ArrayList;

import org.hibernate.SessionFactory;

import com_java_users.Enums.EquipStatus;

public class ResourcesManager extends Equipment implements Seats {
    private Lab lab;
    private String[][] seatDisplay; // seatID and status
    String alpha[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    static int alphaIndex=0; // index of lab
    ArrayList<Equipment> ListOfEquipment = new ArrayList<Equipment>();
    
    //public static SessionFactory sessionFactory;

    public ResourcesManager() {
        super();   
        lab = new Lab(); 
    }
    public Lab getLab() {
        return lab;
    }
    //when a lab is created the seats are created and added into the lab
    public void createLab(String labId, String labName,String location, int seatCapacity){
        this.lab=new Lab(labId,labName,location,seatCapacity);
       addSeats(seatCapacity);
         //create seats(C0) and status(C1) in array and add to lab
    }
    public void addSeats(int seatCapacity) {// develop seating positions
       this.seatDisplay = new String[seatCapacity][2];
        int j=0;
        for(int i=0;i<seatCapacity;i++) {
            seatDisplay[i][j]=alpha[alphaIndex]+i; // seatID
            seatDisplay[i][j+1]=EquipStatus.AVAILABLE.toString(); // status
        }
        alphaIndex++;
        this.lab.setSeatDisplay(seatDisplay);
    }
    
    public void addEquipment(ArrayList<Equipment> equipmentList) {
        ListOfEquipment.addAll(equipmentList);
        lab.setEquipmentList(ListOfEquipment);
    }
    public void reduceEquipment(String equipmentId){
        for(Equipment e: lab.getEquipmentList()){
            if(e.getEquipId().equals(equipmentId)){
                lab.getEquipmentList().remove(e);
            }
        }
    }
    public static void main(String[] args) {
        ResourcesManager rm = new ResourcesManager();
        rm.createLab("L1", "Lab 1", "Room 1", 10);
        ArrayList<Equipment> El = new ArrayList<Equipment>();
        El.add(new Equipment("E1", "Equipment 1", "L1", 10, 10));
        El.add(new Equipment("E2", "Equipment 2", "L1", 10, 10));
        El.add(new Equipment("E3", "Equipment 3", "L1", 10, 10));
        rm.addEquipment(El);
        
        System.out.println(rm.getLab().specificToString());
        for(Equipment e: rm.getLab().getEquipmentList()){
            System.out.println(e.toString());
        }
    }
}