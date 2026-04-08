package driver;
import java.sql.*;



import javax.swing.JOptionPane;

import controller.ReservationOps;
import controller.UserOps;
import model.User;
import model.HibernateConfig;
import model.Lab;
import model.Reservation;
import model.Roles;
import model.SeatRecord;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class Driver {
	
	public static void main(String[] args) {
		
	String url = "jdbc:mysql://localhost:3307/theclebdb";//can be a final string because it doesn't change
	Connection myConn = null;
	try {
		//1. Get Connection to localhost/xampp server
		myConn = DriverManager.getConnection(url, "root", "usbw");//create connection
		if (myConn != null) {
			JOptionPane.showMessageDialog(null,  "Connected to Local Server", "JDBC Connection Status", JOptionPane.INFORMATION_MESSAGE);
		}
		//2. Create a statement
		//User user = new User("l1", "higher ", "dunbarton", "jdoe@my.com", "password123", Roles.ADMIN);
		//UserOps userOps = new UserOps();
		//int rows = userOps.createUserRecord(user);
		
		Reservation rm = new Reservation();
		ReservationOps rmOps = new ReservationOps();
		rmOps.viewReservation(1);
		
		//3. Execute SQL Query
		
		//4. Process the result set
	} catch(Exception e) {
		e.printStackTrace();
	}
	
	

	/*Session session = HibernateConfig.getSessionFactory().openSession();
	Transaction transaction = null;
	transaction = session.beginTransaction();
	
    try {

        // ==========================
        // 1️⃣ Get existing Lab
        // ==========================
        Lab lab = session.get(Lab.class, "L01");

        // ==========================
        // 2️⃣ Get existing Seats
        // ==========================
        SeatRecord seat1 = session.get(SeatRecord.class, "A0");
        SeatRecord seat2 = session.get(SeatRecord.class, "A1");

        // ==========================
        // 3️⃣ Create Reservation
        // ==========================
        Reservation reservation = new Reservation();

        reservation.setUserId("U01");
        reservation.setReservationDate(LocalDate.now());
        reservation.setStartTime(LocalTime.of(9, 0));
        reservation.setEndTime(LocalTime.of(12, 0));
        reservation.setStatus("ACTIVE");
        reservation.setLab(lab);

        // ==========================
        // 4️⃣ Attach Seats
        // ==========================
        List<SeatRecord> seats = new ArrayList<>();
        seats.add(seat1);
        seats.add(seat2);

        reservation.setSeats(seats);

        // ==========================
        // 5️⃣ Persist
        // ==========================
        session.persist(reservation);

        transaction.commit();
        session.close();

        System.out.println("Reservation inserted successfully!");

    } catch (Exception e) {
    	transaction.rollback();
        e.printStackTrace();
    }*/
}
	
	
	
}