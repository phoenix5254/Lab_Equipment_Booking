package controller;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import model.Reservation;
import model.SeatRecord;
import model.Lab;

public class ReservationOps {
	private static SessionFactory sessionFactory = null;
	
	public ReservationOps() {
		sessionFactory = buildSessionFactory();
	}
	
	public static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure().buildSessionFactory();
			
		}catch (Exception e) {
			System.err.println("Session Creation Failed" + e);
			throw new ExceptionInInitializerError(e);
		}
	}
	public void createReservation(String userId,String labId,List<String> seatIds,LocalDate date,LocalTime start,LocalTime end,String status) {

	    Session session = sessionFactory.openSession();
	    Transaction transaction = null;

	    try {
	        transaction = session.beginTransaction();

	        Lab lab = session.get(Lab.class, labId);

	        Reservation reservation = new Reservation();
	        reservation.setUserId(userId);
	        reservation.setReservationDate(date);
	        reservation.setStartTime(start);
	        reservation.setEndTime(end);
	        reservation.setStatus(status);
	        reservation.setLab(lab);

	        List<SeatRecord> seats = new ArrayList<>();

	        for (String seatId : seatIds) {

	            SeatRecord seat = session.get(SeatRecord.class, seatId);

	           
	            seat.setReservation(reservation);   
	            seats.add(seat);
	        }

	        reservation.setSeats(seats); 

	        session.persist(reservation);

	        transaction.commit();

	        System.out.println("Reservation created successfully!");

	    } catch (Exception e) {
	        if (transaction != null) transaction.rollback();
	        e.printStackTrace();
	    } finally {
	        session.close();
	    }
	}
	

    //DELETE WORKS
	public void deleteReservation(int reservationId) {

	    Session session = sessionFactory.openSession();
	    Transaction transaction = null;

	    try {
	    	transaction = session.beginTransaction();

	        Reservation reservation =
	                session.get(Reservation.class, reservationId);

	        if (reservation != null) {
	            session.remove(reservation);
	        }

	        transaction.commit();
	        System.out.println("Reservation deleted!");

	    } catch (Exception e) {
	        if (transaction != null) transaction.rollback();
	        e.printStackTrace();
	    } finally {
	        session.close();
	    }
	}
	
	
	//update reservation works
	public void updateReservation(int reservationId, LocalDate reservationDate, LocalTime newStart,LocalTime newEnd,String newStatus) {

    Session session = sessionFactory.openSession();
    Transaction transaction = null;

    try {
        transaction = session.beginTransaction();

        Reservation reservation =
                session.get(Reservation.class, reservationId);

        if (reservation != null) {
        	reservation.setReservationDate(LocalDate.now());
            reservation.setStartTime(newStart);
            reservation.setEndTime(newEnd);
            reservation.setStatus(newStatus);

            session.merge(reservation);
        }

        transaction.commit();
        System.out.println("Reservation updated!");

    } catch (Exception e) {
        if (transaction != null) transaction.rollback();
        e.printStackTrace();
    } finally {
        session.close();
    }
}
    
    
    public void viewReservation(int reservationId) {

        Session session = sessionFactory.openSession();

        try {

            Reservation reservation = session.get(Reservation.class, reservationId);

            if (reservation != null) {
                System.out.println("Reservation ID: " +
                        reservation.getReservationNum());

                System.out.println("User: " +
                        reservation.getUserId());

                System.out.println("Lab: " +
                        reservation.getLab().getLabId());

                System.out.println("Seats:");

                for (SeatRecord seat : reservation.getSeats()) {
                    System.out.println(" - " + seat.getSeatID());
                }

            } else {
                System.out.println("Reservation not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
   
}