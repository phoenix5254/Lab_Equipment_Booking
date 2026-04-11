package controller;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import model.resource.*;
import model.Enums.EquipStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ResourcesManager extends Equipment {
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

    public boolean createSeatsForLab(Lab lab) {
    Session session = factory.openSession();
    Transaction tx = null;
     try{
         tx = session.beginTransaction();
        for (SeatRecord seat: lab.getSeatDisplay()) {
            session.persist(seat);
        }
        tx.commit();
        JOptionPane.showMessageDialog(null, "Seats created for the lab successfully", "Lab Status",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
     }catch(HibernateException he){
         logger.error("Something went wrong creating the seats for the lab "+he.getMessage());
        JOptionPane.showMessageDialog(null, "Something went wrong creating the seats for the lab: " + he.getMessage(),
                        "Lab Status", JOptionPane.ERROR_MESSAGE);
                        tx.rollback();
     }catch(Exception e){
        logger.error("Something went wrong creating the seats for the lab "+e.getMessage());
        JOptionPane.showMessageDialog(null, "Something went wrong creating the seats for the lab: " + e.getMessage(),
                        "Lab Status", JOptionPane.ERROR_MESSAGE);
                        tx.rollback();
     }finally{
         session.close();
     }
        return false;
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
            if(!createSeatsForLab(lab)) { // create seats for the lab and add to the database
                session.getTransaction().rollback();
                throw new Exception();
            } 
                session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong creating the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createLabWithEquipment(Lab lab, List<Equipment> equipList) {// add equipment to lab immediately when
          
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx=session.beginTransaction();
            for (Equipment equip : equipList) {
                lab.getEquipmentList().add(equip);
            }
            createSeatsForLab(lab);
            session.merge(lab);
           tx.commit();
            logger.info("Lab created with equipment successfully: " + lab.getLabId());
        } catch (Exception e) {
            tx.rollback();
            JOptionPane.showMessageDialog(null, "Something went wrong adding equipment to the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
            logger.error("Something went wrong adding with equipment and or lab " + e.getMessage());
            tx.rollback();
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
            logger.error("Something went wrong updating the lab " + e.getMessage());
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
            logger.error("Something went wrong deleting the lab " + e.getMessage());
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

    public static void initializeResourceData() {
        try (Session session = factory.openSession()) {
           
            Transaction tx = session.beginTransaction();

            // Create 10 labs
            String[] labNames = { "Chemistry Lab", "Physics Lab", "Biology Lab", "Computer Lab", "Engineering Lab",
                    "Math Lab", "History Lab", "Art Lab", "Music Lab", "Sports Lab" };
            String[] locations = { "Building A", "Building B", "Building C", "Building D", "Building E",
                    "Building F", "Building G", "Building H", "Building I", "Building J" };
            int[] capacities = { 20, 25, 15, 30, 18, 12, 10, 22, 14, 28 };

            for (int i = 0; i < 10; i++) {
                String labId = String.format("L%02d", i + 1);
                Lab lab = new Lab(labId, labNames[i], locations[i], capacities[i]);
                session.persist(lab);

                // Create different types of equipment for each lab
                String[] equipTypes = { "Microscope", "Computer", "Beaker", "Oscilloscope", "Petri Dish",
                        "Projector", "Calculator", "Paint Brush", "Piano", "Treadmill" };
                for (int j = 0; j < 5; j++) { // 5 equipment per lab
                    String equipId = String.format("E%02d%02d", i + 1, j + 1);
                    Equipment equip = new Equipment(equipId, equipTypes[j], lab, EquipStatus.AVAILABLE, 10, 10);
                    session.persist(equip);
                }
            }

            tx.commit();
            JOptionPane.showMessageDialog(null, "Database initialized with 10 labs and equipment",
                    "Initialization Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error initializing data: " + e.getMessage(), "Initialization Status",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        ResourcesManager manager = new ResourcesManager();
        initializeResourceData();
    }

}