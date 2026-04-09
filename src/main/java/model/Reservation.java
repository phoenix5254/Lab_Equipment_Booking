package model;

import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import model.Lab;
import model.SeatRecord;
import model.Equipment;

@Entity
@Table(name = "reservations")
public class Reservation implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	
    private int reservationNum;
    private String userId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
//
    @ManyToOne
    @JoinColumn(name = "labId")
    private Lab lab;
    
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<SeatRecord> seats;

    @ManyToMany
    @JoinTable(
        name = "reservation_equipment",
        joinColumns = @JoinColumn(name = "reservationNum"),
        inverseJoinColumns = @JoinColumn(name = "equipmentID")
    )
    private List<Equipment> equipmentList;

    
    public Reservation(int reservationNum, String userId, LocalDate reservationDate, LocalTime startTime,
			LocalTime endTime, String status, Lab lab, List<SeatRecord> seats, List<Equipment> equipmentList) {
		this.reservationNum = reservationNum;
		this.userId = userId;
		this.reservationDate = reservationDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.lab = lab;
		this.seats = seats;
		this.equipmentList = equipmentList;
	}
    
    public Reservation() {
        this.seats = new ArrayList<>();
        this.equipmentList = new ArrayList<>();
    }

	

    public long getReservationNum() {
        return reservationNum;
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

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public List<SeatRecord> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatRecord> seats) {
        this.seats = seats;
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

	@Override
	public String toString() {
		return "Reservation [reservationNum=" + reservationNum + ", userId=" + userId + ", reservationDate="
				+ reservationDate + ", startTime=" + startTime + ", endTime=" + endTime + ", status=" + status
				+ ", lab=" + lab + ", seats=" + seats + ", equipmentList=" + equipmentList + "]";
	}
    
}