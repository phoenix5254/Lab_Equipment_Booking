package model.resource;



public class EquipmentReserved {
    public int reservationNum;
    private String equipmentID;
    private int equipmentQty;

    public EquipmentReserved() {
        reservationNum =0;
        equipmentID = "";
        equipmentQty = 0;
    }
    public EquipmentReserved(int reservationNum, String equipmentID, int equipmentQty) {
        setReservationNum(reservationNum);
        setEquipmentID(equipmentID);
        setEquipmentQty(equipmentQty);
    }

    private int getReservationNum() {
        return reservationNum;
    }
    private void setReservationNum(int reservationNum) {
        this.reservationNum = reservationNum;
    }
    public String getEquipmentID() {
        return equipmentID;
    }
    private void setEquipmentID(String equipmentID) {
        this.equipmentID = equipmentID;
    }
    public int getEquipmentQty() {
        return equipmentQty;
    }
    public void setEquipmentQty(int equipmentQty) {
        this.equipmentQty = equipmentQty;
    }
    @Override
    public String toString() {
        return "EquipmentReserved:"+"/nReservationNum:" + getReservationNum() + "\n EquipmentID:"+ getEquipmentID() + "\nEquipmentQty:" + getEquipmentQty() + "\n";
    }
    

   
}