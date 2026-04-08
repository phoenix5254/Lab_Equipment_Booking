package model;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;



public class ResourcesManager extends Equipment  {
    private Lab lab;
    private final Logger logger = LogManager.getLogger(ResourcesManager.class);

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

    public void createEquipment(Equipment equipment) {// create equipment seprately
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
            logger.warn("Equipment deleted with ID: " + equip.getEquipId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong deleting the equipment: " + e.getMessage(),
                    "Equipment Status", JOptionPane.ERROR_MESSAGE);
        }
    }
    public Equipment readEquipment(String equipId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Equipment equip = session.get(Equipment.class, equipId);
            return equip;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong reading the equipment: " + e.getMessage(),
                    "Equipment Status", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    public Lab getLab() {
        return lab;
    }

    // when a lab is created the seats are created and added into the lab
    public void createLabOnly(Lab lab) throws SQLException {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            session.persist(lab); // create database entry for the lab
            lab.addSeats(lab);// create database entries for seats in the lab
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong creating the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void createLabWithEquipment(Lab lab, List<Equipment> equipList) {//add equipment to lab immediately when creating the lab
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            for(Equipment equip: equipList) {
                lab.getEquipmentList().add(equip);
            }
            lab.addSeats(lab);       
            session.merge(lab);
            session.getTransaction().commit();
            logger.info("Lab created with equipment successfully: " + lab.getLabId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong adding equipment to the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
                    logger.error("Something went wrong adding with equipment and or lab "+e.getMessage());
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
                        logger.error("Something went wrong updating the lab "+e.getMessage());
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
                    logger.error("Something went wrong deleting the lab "+e.getMessage());
        }
    }
    public Lab readLab(String labId) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            Lab lab = session.get(Lab.class, labId);
            if (lab != null) {
                
                return lab;
            } else {
                JOptionPane.showMessageDialog(null, "Lab not found with ID: " + labId, "Lab Status",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong reading the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
        return null;
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
            session.createNativeQuery("TRUNCATE TABLE equipment").executeUpdate();
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
        Equipment equip1 = new Equipment("E01", "Mie", lab1, EquipStatus.AVAILABLE, 10, 10);
        rm.createLabOnly(lab1);
        rm.createEquipment(equip1);

        lab1= new Lab("m2","uy","bu",15);
        List<Equipment> equipList = new ArrayList<Equipment>();
        Equipment equip2 = new Equipment("E02", "Mie", lab1, EquipStatus.AVAILABLE, 15, 15);
        equipList.add(equip2);
        Equipment equip3 = new Equipment("E03", "Mie", lab1, EquipStatus.AVAILABLE, 15, 15);
        equipList.add(equip3);
        Equipment equip4 = new Equipment("E04", "Mie", lab1, EquipStatus.AVAILABLE, 15, 15);
        equipList.add(equip4);
        rm.createLabWithEquipment(lab1, equipList);

       /*  rm.deleteLab(rm.readLab("L01"));
        rm.deleteEquipment(rm.readEquipment("E02"));
        Lab lab1=rm.readLab("m2");

        Equipment equip3 = new Equipment("E03", "Mie", lab1, EquipStatus.AVAILABLE, 15, 15);
        rm.updateEquipment(equip3);
        lab1.setLabName("tye");
        rm.updateLab(lab1);*/
        
     
        

    }

}