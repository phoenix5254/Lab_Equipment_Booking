package model.resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;

import model.users.Driver;

public class ResourcesManager extends Equipment implements Seats {
    private Lab lab;

    public static SessionFactory factory = null;

    public ResourcesManager() {
        super();
        lab = new Lab();
        factory = buildSessionFactory();
        
    }

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void createEquipment(Equipment equipment) {
        Session session = factory.openSession();
        session.beginTransaction();
        session.persist(equipment);
        session.getTransaction().commit();
        session.close();
    }
    public void updateEquipment(Equipment equip) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.merge(equip);
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong updating the equipment: " + e.getMessage(),
                    "Equipment Status", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void deleteEquipment(Equipment equip) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.remove(equip);
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong deleting the equipment: " + e.getMessage(),
                    "Equipment Status", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void readEquipment(String equipId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Equipment equip = session.get(Equipment.class, equipId);
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong reading the equipment: " + e.getMessage(),
                    "Equipment Status", JOptionPane.ERROR_MESSAGE);
        }
    }
    public Lab getLab() {
        return lab;
    }

    // when a lab is created the seats are created and added into the lab
    public void createLab(Lab lab) throws SQLException {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.persist(lab);
            addSeats(lab);// create database entries for seats in the lab
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong creating the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void addSeats(Lab lab) {
        Connection myConn = Driver.getConnection();
        {
            for (int seatNum = 0; seatNum < lab.getSeatCapacity(); seatNum++) {
                SeatRecord newSeat = new SeatRecord(lab, seatNum);
                String sql = "INSERT INTO seatsRecords (seatID, status, lab) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
                    pstmt.setString(1, newSeat.getSeatID());
                    pstmt.setString(2, newSeat.getStatus());
                    pstmt.setString(3, newSeat.getLabName().getLabId());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Insertion failed: " + e.getMessage(), "Insert Status",
                            JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException("Failed to add seat", e);
                }
            }
        }
    }
    public void updateLab(Lab lab) {
            try (Session session = factory.openSession()) {
                session.beginTransaction();
                session.merge(lab);
                session.getTransaction().commit();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Something went wrong updating the lab: " + e.getMessage(),
                        "Lab Status", JOptionPane.ERROR_MESSAGE);
            }
    }
    public void deleteLab(Lab lab) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.remove(lab);
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong deleting the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void readLab(String labId) {
        try (Session session = factory.openSession()) {
            Lab lab = session.get(Lab.class, labId);
            if (lab != null) {
                JOptionPane.showMessageDialog(null, "Lab found with ID: " + labId, "Lab Status",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Lab not found with ID: " + labId, "Lab Status",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong reading the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
    }

 public static void truncateDatabase() {
    // Use the existing session from the try-with-resources
    try (Session session = factory.openSession()) {
        Transaction tx = session.beginTransaction();
        
        try {
            // Execute all queries within the SAME session and transaction
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE seatsRecords").executeUpdate();
            session.createNativeQuery("TRUNCATE TABLE labs").executeUpdate();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e; // Rethrow to be caught by the outer catch block
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, 
            "Something went wrong truncating the database: " + e.getMessage(),
            "Database Status", JOptionPane.ERROR_MESSAGE);
    }
    JOptionPane.showMessageDialog(null, "Database truncated successfully", "Database Status", JOptionPane.INFORMATION_MESSAGE);
}

    

    
    public static void main(String[] args) throws SQLException {
        ResourcesManager rm = new ResourcesManager();
        truncateDatabase();
        Lab lab1 = new Lab("L01", "Ch", "Bu", 10);
        Equipment equip1 = new Equipment("E01", "Mie", lab1, 10, 10);
        
        rm.createLab(lab1);
        rm.createEquipment(equip1);
        

    }

}