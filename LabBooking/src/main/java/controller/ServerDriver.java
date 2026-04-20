package controller;

import java.io.*;
import model.resource.Reservation;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import model.resource.Equipment;
import model.resource.Lab;
import model.users.User;

public class ServerDriver implements Serializable {

	private static final long serialVersionUID = 1L;
	private ServerSocket serverSocket;
    private static Connection myConn;

    public ServerDriver() {
        try {
            serverSocket = new ServerSocket(8888, 30);
            myConn = getDatabaseConnection();

            System.out.println("MAIN SERVER STARTED...");

            waitForRequests();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForRequests() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getDatabaseConnection() {
        try {
            if (myConn == null) {
                myConn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/equipmentbooking",
                        "root",
                        ""
                );

                JOptionPane.showMessageDialog(null, "DB Connected");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return myConn;
    }
    
    

    public static class ClientHandler implements Runnable {

        private Socket socket;
        private ObjectInputStream objIs;
        private ObjectOutputStream objOs;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
        	try {
                objOs = new ObjectOutputStream(socket.getOutputStream());

                objIs = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Object input = objIs.readObject();
                    if (input == null) {
                        break;
                    }
                    String action = (String) input;
                    /*
                     * ==========================
                     * USER MANAGER ACTIONS
                     * ==========================
                     */
                    if (action.equals("Create User")) {

                        User user = (User) objIs.readObject();

                        int rows = UserManager.createUserRecord(user);
                        if (rows == 1) {
                        	objOs.writeObject(true);
                        }else {
                        	objOs.writeObject(false);
                        }

                    } else if (action.equals("Find User")) {

                        String userID = (String) objIs.readObject();

                        User user = UserManager.findUserById(userID);

                        objOs.writeObject(user);

                    } else if (action.equals("Update User")) {

                        User user = (User) objIs.readObject();

                        int rows = UserManager.updateUser(user);

                        objOs.writeObject(rows == 1);

                    } else if (action.equals("Delete User")) {

                        String userID = (String) objIs.readObject();

                        int rows = UserManager.deleteUser(userID);

                        objOs.writeObject(rows == 1);
                    }

                    /*
                     * ==========================
                     * RESOURCE MANAGER ACTIONS
                     * ==========================
                     */
                    // edited below
                    else if (action.equals("Create Lab")) {

                        Lab lab = (Lab) objIs.readObject();
                        ResourcesManager.createLabOnly(lab); // lab is created with seats
                        objOs.writeObject(true);
                        // edited above
                    } else if (action.equals("Edit Lab")) {
                        String labId = (String) objIs.readObject();
                        Lab lab = ResourcesManager.readLab(labId);
                        objOs.writeObject(lab);

                    } else if (action.equals("Update Lab")) {

                        Lab lab = (Lab) objIs.readObject();

                        ResourcesManager.updateLab(lab);

                        objOs.writeObject(true);

                    } else if (action.equals("Delete Lab")) {

                        String labId = (String) objIs.readObject();

                        Lab lab = ResourcesManager.readLab(labId);

                        ResourcesManager.deleteLab(lab);

                        objOs.writeObject(true);

                    } else if (action.equals("Create Equipment")) {
                        Equipment eq = (Equipment) objIs.readObject();
                        // Capture the success/failure boolean from the manager
                        boolean success = ResourcesManager.createEquipment(eq);
                        objOs.writeObject(success); // Always send a response back to the client

                    } else if (action.equals("Edit Equipment")) {

                        String eqId = (String) objIs.readObject();

                        Equipment eq = ResourcesManager.readEquipment(eqId);

                        objOs.writeObject(eq);

                    } else if(action.equals("Update Equipment")){
                        Equipment eq = (Equipment) objIs.readObject();
                        boolean done = ResourcesManager.updateEquipment(eq);
                        objOs.writeObject(done);
                    }
                    else if (action.equals("Delete Equipment")) {
                    	
                        String eqId = (String) objIs.readObject();

                        Equipment eq = ResourcesManager.readEquipment(eqId);

                        ResourcesManager.deleteEquipment(eq);

                        objOs.writeObject(true);
                    }

                    // ==========================
                    // RESERVATION MANAGER ACTIONS
                    // ==========================
                    // */

                    else if (action.equals("Create Reservation")) {
                    	System.out.println("SERVER: Create Reservation action recieved");
                        Reservation reservation = (Reservation) objIs.readObject();
                        System.out.println("SERVER: Reservation recieved");
                        System.out.println("SERVER: Calling reservation manager");
                        ReservationManager rm = new ReservationManager();
                        
                        int rows = rm.createReservation(reservation);
                        System.out.println("SERVER: Printing Rows: " + rows);
                        if (rows > 0) {
                        	objOs.writeObject(true);
                        }else {
                        	objOs.writeObject(false);
                        }
                        
                    } else if (action.equals("Get all labs")) {
                    	System.out.println("SERVER: RECIEVED GET ALL LABS REQUEST");
                        List<Lab> labs = ReservationManager.getAllLabs();
                        System.out.println("SERVER: SENT LABS");
                        objOs.writeObject(labs);

                } else if (action.equals("Get Equipment By Lab")) {
                	System.out.println("SERVER: Receievd Request Equipment By Lab");
                	Lab lab = (Lab) objIs.readObject();
                	System.out.println("SERVER: Calling Reservation Manager Equipment By Lab");
                	List<Equipment> equipList = ReservationManager.getEquipmentByLabId(lab.getLabId());
        		    System.out.println("after calling reservation manager");
        		    objOs.writeObject(equipList);
        		    
                }else if (action.equals("Get Next Reservation Num")){
                	System.out.println("SERVER: Receieved Request Get Next Reservation Num");
                	int resNum = ReservationManager.getnextReservationNum();
                	objOs.writeObject(resNum);
                	
                } else if (action.equals("Get Reservations By Lab")) {
                    System.out.println("SERVER: Get Reservations By Lab Action received");
                    String labId = (String) objIs.readObject();
                    System.out.println("SERVER: Recieved labId = " + labId);
                    List<Reservation> reservations =
                            ReservationManager.getReservationsByLabId(labId);
                    System.out.println("SERVER: No. of Reservations Found = " + reservations.size());
                    objOs.writeObject(reservations);
                    objOs.flush();
                    System.out.println("SERVER: Reservations Sent");
                    
                } else if (action.equals("Search Reservations")) {

                    String keyword = (String) objIs.readObject();
                    String column = (String) objIs.readObject();

                    System.out.println("SERVER: Received " + keyword + " and " + column);
                    
                    ReservationManager rm = new ReservationManager();
                    List<Reservation> results = rm.getReservations(column, keyword);

                    objOs.writeObject(results);

                    System.out.println("SERVER: Sent reservation list to client");
               
                //added for save button in admin view
                }else if(action.equals("Update Reservation Status")) {
                	    int reservationNum = (int) objIs.readObject();
                	    String status = (String) objIs.readObject();
                	    ReservationManager rm = new ReservationManager();
                	    rm.updateReservationStatus(reservationNum, status);
                	    
                }else if(action.equals("Get User Reservations")) {
                		String userId = (String) objIs.readObject();
                	    ReservationManager rm = new ReservationManager();
                		List<Reservation> reservList = rm.getReservationsByUserId(userId);
                		objOs.writeObject(reservList);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "NO SERVER RESPONSE FOR ACTION: " + action);
                }
                objOs.flush();
            	}
        	} catch (SocketException e) {
                System.out.println("CLIENT DISCONNECTED FROM NETWORK");
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                try {
                	if (objOs != null) objOs.close();
                	if (objIs != null) objIs.close();
                	if (socket != null) socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
          
       }
    }
        
    public static void main(String[] args) {
        new ServerDriver();
    }
}

