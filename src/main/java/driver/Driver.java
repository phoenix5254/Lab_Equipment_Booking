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
import java.util.Arrays;
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
//		User user = new User("U02", "higher ", "dunbarton", "jdoe@my.com", "password123", Roles.ADMIN);
//		UserOps userOps = new UserOps();
//		int rows = userOps.createUserRecord(user);
		ReservationOps rmOps = new ReservationOps();

		List<String> seatIds = Arrays.asList("A0");

		rmOps.createReservation("U04","m2",seatIds,LocalDate.now(),LocalTime.of(9, 0),LocalTime.of(12, 0),"ACTIVE");
		
        rmOps.viewReservation(27);
		
		rmOps.updateReservation(26,LocalDate.now(),LocalTime.of(7, 0), LocalTime.of(12, 0), "ACTIVE");
        
        rmOps.deleteReservation(6);
		
	} catch(Exception e) {
		e.printStackTrace();
	}
	
	
}
	
	
	
}