package controller;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import model.Enums.EquipStatus;
import model.resource.*;

public class ReservationManager {
    private static SessionFactory sessionFactory = null;

    public ReservationManager() {
        sessionFactory = buildSessionFactory();
    }

    public static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();

        } catch (Exception e) {
            System.err.println("Session Creation Failed" + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private Reservation findReservationByNumber(int reservationNum) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Reservation reservation = null;
        try {
            tx = session.beginTransaction();
            reservation = session.get(Reservation.class, reservationNum);
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return reservation;
    }

    private void releaseResources(Reservation reservation) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
        if (reservation == null) {
            JOptionPane.showMessageDialog(null, "Release Reservation Resources", "Error Reservation object is null", JOptionPane.ERROR_MESSAGE);
            throw new Exception("Release Reservation Resources: Reservation object is null");
        }
        SeatRecord seat = reservation.getSeat();
        if (seat != null) {
            seat.setStatus(EquipStatus.AVAILABLE.toString());
            seat.setReservation(null);
            session.merge(seat);
        }else{
            JOptionPane.showMessageDialog(null, "No seat to release", "Release Resources", JOptionPane.INFORMATION_MESSAGE);
            throw new Exception("No seat to release");
        }
        if (reservation.getEquipmentList() != null) {
            for (Equipment equipment : reservation.getEquipmentList()) {
                if (equipment == null) {
                    continue;
                }
                int qtyAvailable = equipment.getQtyAvailable();
                equipment.setQtyAvailable(qtyAvailable + 1);
                session.merge(equipment);
            }
        }
        tx.commit();
    }catch (Exception e) {
        if (tx != null)
            tx.rollback();
        e.printStackTrace();
    } finally {
        session.flush();
        session.close();
    }
}

    private void reserveResources(Reservation reservation) {
		if (reservation == null) {
			return;
		}
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
        //Confirms seat
		SeatRecord seat = reservation.getSeat();
		if (seat.getStatus() != EquipStatus.BOOKED.toString()) {
			seat.setStatus(EquipStatus.BOOKED.toString());
			seat.setReservation(reservation);
			session.merge(seat);
            //loop to sellect equipment and update qty on hand and available
			for (Equipment equipment : reservation.getEquipmentList()) {
				if (equipment == null) {
					continue;
				}
				int qtyAvailable = equipment.getQtyAvailable() ;
				if (qtyAvailable > 0) {
					equipment.setQtyOnHand(qtyAvailable - 1);
					session.merge(equipment);
				}else{
                    JOptionPane.showMessageDialog(null, "Not enough equipment available", "Error", JOptionPane.ERROR_MESSAGE);
                    throw new Exception("Not enough equipment available");
                }
            }
		}else {
            JOptionPane.showMessageDialog(null, "Seat is already booked", "Error", JOptionPane.ERROR_MESSAGE);
            throw new Exception("Seat is already booked");
        }
        tx.commit();
    }catch (Exception e) {
        if (tx != null)
            tx.rollback();
        e.printStackTrace();
    } finally {
        session.flush();
        session.close();
    }
}
    public void createReservation(Reservation reservation) {

	    Session session = sessionFactory.openSession();
	    Transaction transaction = null;

	    try {
	        transaction = session.beginTransaction();
	        Lab lab = session.get(Lab.class, reservation.getLab().getLabId());
	        SeatRecord seat = session.get(SeatRecord.class, reservation.getSeat().getSeatID());
			seat.setStatus(EquipStatus.BOOKED.toString());
			reservation.setSeat(seat);
			session.merge(seat);
            List<Equipment> reservedEquipment = new ArrayList<>(lab.getEquipmentList());
            if (equipmentIds != null && !equipmentIds.isEmpty()) {
                for (String equipId : equipmentIds) {
                    Equipment equipment = session.get(Equipment.class, equipId);
                    if (equipment == null) {
                        continue;
                    }
                    int currentQtyOnHand = equipment.getQtyOnHa();
                    if (currentQtyOnHand > 0) {
                        equipment.setQtyOnHand(currentQtyOnHand - 1);
                        int currentQtyAvailable = equipment.getQtyAvailable() ;
                        equipment.setQtyAvailable(Math.max(0, currentQtyAvailable - 1));
                        session.merge(equipment);
                        reservedEquipment.add(equipment);
                    }
                }
            }
            reservation.setEquipmentList(reservedEquipment);

            session.persist(reservation);
            if (seat != null) {
                session.merge(seat);
            }
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
    // DELETE WORKS
    public void deleteReservation(int reservationId) {

        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Reservation reservation = findReservationByNumber(reservationId);

            if (reservation != null) {
                releaseResources(reservation);
                session.remove(reservation);
            }else {
                JOptionPane.showMessageDialog(null, "Reservation not found.", "Error", JOptionPane.ERROR_MESSAGE);
                throw new Exception("Reservation not found.");
            }

            transaction.commit();
            JOptionPane.showMessageDialog(null, "Reservation deleted successfully!");

        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
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
        Reservation reservation = session.get(Reservation.class, reservationId);

        if (reservation != null) {
        	reservation.setReservationDate(LocalDate.now());
            reservation.setStartTime(newStart);
            reservation.setEndTime(newEnd);
            reservation.setStatus(newStatus);
            session.merge(reservation);
        }
        transaction.commit();
        JOptionPane.showMessageDialog(null, "Reservation updated successfully!");

    } catch (Exception e) {
        if (transaction != null) transaction.rollback();
        e.printStackTrace();
    } finally {
        session.close();
    }
}
    public void viewReservation(int reservationId) {
            Reservation reservation = findReservationByNumber(reservationId);
            if (reservation != null) {
                JOptionPane.showMessageDialog(null, "Reservation Details:\n" +
                        "Reservation ID: " + reservation.getReservationNum() + "\n" +
                        "User: " + reservation.getUserId() + "\n" +
                        "Date: " + reservation.getReservationDate() + "\n" +
                        "Time: " + reservation.getStartTime() + " - " + reservation.getEndTime() + "\n" +
                        "Lab: " + reservation.getLab().getLabId() + "\n" +
                        "Seats: " + (reservation.getSeat() != null ? reservation.getSeat().getSeatID() : "N/A") + "\n" +
                        "Status: " + reservation.getStatus(), "Reservation Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Reservation not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

