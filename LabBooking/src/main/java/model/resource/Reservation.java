package model.resource;

import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;


@Entity
@Table(name = "reservations")
@IdClass(ReservationId.class)
public class Reservation implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    private int reservationNum;
    private String userId;
    private LocalDate reservationDate;
    @Id
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
//
    @ManyToOne
    @JoinColumn(name = "labId")
    private Lab lab;
    
    @OneToOne
    private SeatRecord seat;

    @ManyToMany
    @JoinTable(
        name = "reservation_equipment",
        joinColumns = {
            @JoinColumn(name = "reservationNum"),
            @JoinColumn(name = "startTime")
        },
        inverseJoinColumns = @JoinColumn(name = "equipmentID")
    )
    private List<Equipment> equipmentList;

    
    public Reservation(int reservationNum, String userId, LocalDate reservationDate, LocalTime startTime,
			LocalTime endTime, String status, Lab lab, SeatRecord seat, List<Equipment> equipmentList) {
		this.reservationNum = reservationNum;
		this.userId = userId;
		this.reservationDate = reservationDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.lab = lab;
		this.seat = seat;
		this.equipmentList = equipmentList;
	}
    public Reservation(String userId, LocalDate reservationDate, LocalTime startTime,
            LocalTime endTime, String status, Lab lab) {
        this.userId = userId;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.lab = lab;
    }
    
    public Reservation() {
        this.seat = new SeatRecord();
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

    public SeatRecord getSeat() {
        return seat;
    }

    public void setSeat(SeatRecord seat) {
        this.seat = seat;
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
				+ ", lab=" + lab + ", seat=" + seat + ", equipmentList=" + equipmentList + "]";
	}
    
}