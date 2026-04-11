package model.resource;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationId)) return false;
        ReservationId that = (ReservationId) o;
        return reservationNum == that.reservationNum &&
                Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationNum, startTime);
    }

    @Override
    public String toString() {
        return "ReservationId [reservationNum=" + reservationNum + ", startTime=" + startTime + "]";
    }
    
   
}
