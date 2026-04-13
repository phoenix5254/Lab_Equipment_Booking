package model.users;
import java.awt.HeadlessException;
import java.sql.*;
import javax.swing.JOptionPane;

import view.*;
public class Validation {
	private static Connection myConn = null;
	private String createSql = "";
	private Statement stmt = null;
	private ResultSet result = null;
	
	public Validation() {
		Driver.connectDatabse();
	}
	
	public int registerUser(User user) {
//		CRUD - INSERT UPDATE DELETE AND SELECT
		//create statement returns a statement obj which 
		createSql = "INSERT INTO usersDB.users (id, firstName, lastName) VALUES ('"+ user.getUserID() +"','" + user.getFirstName() +"','" + user.getLastName()+"');";
		System.out.println(createSql);
		int affectedRows = 0;
		try {
			//stmt = myConn.createStatement("Insert"); then same execute update method
			stmt = myConn.prepareStatement(createSql);//prevents sql injection
			affectedRows = stmt.executeUpdate(createSql);//returns an integer - no. of rows of data
			//used for insert, update and delete
//			result = stmt.executeQuery(createSql) - for select statements and returns a result set
//			Based on no. of records in result set tells us how we should treat it
			
//			if (result.next()) {
//				User user = new User();
//				user.setId(result.getInt(1));
//				user.setFirstName(result.getString("name"));
//				user.setEmail(result.getEmail("email"));
				
//				return user;
						
//				user = new (id, name, email);
				//for multiple users - create a list outside add user to list 
				//change if to while 
//			}
			
		}catch (SQLException e)
		{
//			e.printStackTrace();
			System.err.println("Error in entering record.");
//			try {
//				JOptionPane jOptionPane = new JOptionPane();
				JOptionPane.showMessageDialog(gui.parentFrame, "Error in entry", null, JOptionPane.INFORMATION_MESSAGE);
//			} catch (HeadlessException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
		catch (Exception e)
		{
			System.err.println("Invalid entry.");
		}
		return affectedRows;
		
	}
	
	public int loginUser(User user) {
//		CRUD - INSERT UPDATE DELETE AND SELECT
		//create statement returns a statement obj which 
		createSql = "SELECT * FROM usersDB.users (userId, password) WHERE userId ==  ('"+user.getUserID() +"');";
		System.out.println(createSql);
		int affectedRows = 0;
		try {
			//stmt = myConn.createStatement("Insert"); then same execute update method
			result = stmt.executeQuery(createSql); //- for select statements and returns a result set
//			stmt = myConn.prepareStatement(createSql);//prevents sql injection
			affectedRows = stmt.executeUpdate(createSql);//returns an integer - no. of rows of data
			//used for insert, update and delete
//			Based on no. of records in result set tells us how we should treat it
			
//			if (result.next()) {
//				User user = new User();
//				user.setId(result.getInt(1));
//				user.setFirstName(result.getString("name"));
//				user.setEmail(result.getEmail("email"));
				
//				return user;
						
//				user = new (id, name, email);
				//for multiple users - create a list outside add user to list 
				//change if to while 
//			}
			
		}catch (SQLException e)
		{
//			e.printStackTrace();
			System.err.println("Error in retrieving record.");
//			try {
//				JOptionPane jOptionPane = new JOptionPane();
				JOptionPane.showMessageDialog(gui.parentFrame, "Error in retrieval", null, JOptionPane.INFORMATION_MESSAGE);
//			} catch (HeadlessException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
		catch (Exception e)
		{
			System.err.println("Invalid entry.");
		}
		return affectedRows;
		
	}
	
	/*public static Connection getConnection() {
		String url = "jdbc:mysql://localhost:3307/usersdb";
		if (myConn == null) {
			try {
				myConn = DriverManager.getConnection(url, "root", "usbw");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return myConn;
	}*/
}