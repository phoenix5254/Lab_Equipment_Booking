package controller;

import java.time.*;
import javax.swing.JOptionPane;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import model.Reservation;
import model.SeatRecord;
import model.EquipStatus;
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
	public void createReservation(String userId,String labId,String seatId,LocalDate date,LocalTime start,LocalTime end,String status) {

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


	        SeatRecord seat = session.get(SeatRecord.class, seatId);
			seat.setStatus(EquipStatus.BOOKED.toString());
			reservation.setSeat(seat);
			session.merge(seat);
	        session.persist(reservation);
	        transaction.commit();

	        JOptionPane.showMessageDialog(null, "Reservation created successfully!");

	    } catch (Exception e) {
	        if (transaction != null) transaction.rollback();
	        e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to create reservation: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
	
	
	//Admins Only
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
				JOptionPane.showMessageDialog(null, "Reservation Details:\n" +
						"Reservation ID: " + reservation.getReservationNum() + "\n" +
						"User: " + reservation.getUserId() + "\n" +
						"Date: " + reservation.getReservationDate() + "\n" +
						"Time: " + reservation.getStartTime() + " - " + reservation.getEndTime() + "\n" +
						"Lab: " + reservation.getLab().getLabId() + "\n" +
						"Seats: " + reservation.getSeat().getSeatID() + "\n" +
						"Status: " + reservation.getStatus(), "Reservation Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
				JOptionPane.showMessageDialog(null, "Reservation not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
   
}