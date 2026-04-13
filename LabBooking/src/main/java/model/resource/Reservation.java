package model.resource;

import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import controller.ReservationManager;
import controller.ResourcesManager;

public class Reservation implements Serializable {
	private static final long serialVersionUID = 1L;
    private int reservationNum;
    private String userId;
    private String seatId;
    private String labId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private List<Equipment> equipmentList;
    private int[] equipmentQty;

    static int reservationCounter = 0;

     public Reservation() {
        this.reservationNum =0;
        this.userId = "";
        this.reservationDate = LocalDate.now();
        this.startTime = LocalTime.now();
        this.endTime = LocalTime.now().plusHours(1);
        this.status = "UNKNOWN";
        this.labId = "";
        this.seatId = "";
        this.equipmentList = new ArrayList<>();
        equipmentQty = null;
    }

    public Reservation(String userId, LocalDate reservationDate, LocalTime startTime,
			LocalTime endTime, String status, String labId, String seatId, List<Equipment> equipmentList,int[] equipmentQty, int resNum) {
		this.reservationNum = ++reservationCounter;
		this.userId = userId;
		this.reservationDate = reservationDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.labId = labId;
		this.seatId = seatId;
		this.equipmentList = equipmentList;
        this.reservationNum = resNum;
        this.equipmentQty = equipmentQty;
	}
     public Reservation(String userId, LocalDate reservationDate, LocalTime startTime,
			LocalTime endTime, String status, String labId, String seatId,int[] equipmentQty, int resNum) {
		this.reservationNum = ++reservationCounter;
		this.userId = userId;
		this.reservationDate = reservationDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.labId = labId;
		this.seatId = seatId;
		this.equipmentList = new ArrayList<>();
        this.reservationNum = resNum;
        this.equipmentQty = equipmentQty;
	}
    public Reservation(Reservation reservation) {
        this.userId = reservation.userId;
        this.reservationDate = reservation.reservationDate;
        this.startTime = reservation.startTime;
        this.endTime = reservation.endTime;
        this.status = reservation.status;
        this.labId = reservation.getLabId();
        this.seatId = reservation.getSeatId();
        this.equipmentList = reservation.getEquipmentList();
        this.reservationNum = reservation.getReservationNum();
        this.equipmentQty = reservation.getEquipmentQty();
    }

    public int getReservationNum() {
        return reservationNum;
    }
    public int[] getEquipmentQty() {
        return equipmentQty;
    }

    public void setEquipmentQty(int[] equipmentQty) {
        this.equipmentQty = equipmentQty;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeat(String seat) {
        this.seatId = seat;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Equipment getEquipment(String Equipid) {
        for (int i = 0; i < equipmentList.size(); i++) {
            if (equipmentList.get(i).getEquipId().equals(Equipid)) {
                ResourcesManager eR= new ResourcesManager();
                return eR.readEquipment(Equipid);
            }
        }
        return null;
        
    }

	@Override
	public String toString() {
		return "Reservation\nReservationNum:" + reservationNum + "\n userId:" + userId + "\n reservationDate:"
				+ reservationDate + "\n startTime:" + startTime + "\n endTime:" + endTime + "\n status:" + status
				+ "\n lab:" + labId + "\n seat:" + seatId + "\nEquipment List: "+ "\n" + ReservationManager.getReservationEquipmentString(reservationNum);
	}
    
}