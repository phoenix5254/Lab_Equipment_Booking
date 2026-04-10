package model;

import java.io.Serializable;
import java.time.LocalTime;

public class ReservationId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int reservationNum;
    private LocalTime startTime;
    
    public ReservationId() {
    }
    
    public ReservationId(int reservationNum, LocalTime startTime) {
        this.reservationNum = reservationNum;
        this.startTime = startTime;
    }
    
    public int getReservationNum() {
        return reservationNum;
    }
    
    public void setReservationNum(int reservationNum) {
        this.reservationNum = reservationNum;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "ReservationId [reservationNum=" + reservationNum + ", startTime=" + startTime + "]";
    }
    
   
}
