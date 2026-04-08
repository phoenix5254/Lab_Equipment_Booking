package controller;

import javax.swing.JOptionPane;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import jakarta.persistence.JoinColumn;
import model.Reservation;
import model.HibernateConfig;
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

    // ADD
    public void addReservation(Reservation reservation) {

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();
            session.persist(reservation);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // UPDATE
    public void updateReservation(Reservation reservation) {

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();
            session.merge(reservation);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteReservation(String reservationId) {

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            Reservation reservation =
                    session.get(Reservation.class, reservationId);

            if (reservation != null) {
                session.remove(reservation);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    //view reservation return object of search
    public Reservation viewReservation(int reservationNum) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Reservation rs = session.get(Reservation.class, reservationNum);//
            if (rs != null) {
                
                return rs;
            } else {
                JOptionPane.showMessageDialog(null, "Lab not found with ID: " + reservationNum, "Lab Status",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong reading the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
   
}