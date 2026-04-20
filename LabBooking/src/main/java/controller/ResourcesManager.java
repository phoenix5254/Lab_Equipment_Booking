package controller;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import model.resource.*;
import model.users.User;
import model.Enums.EquipStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ResourcesManager extends Equipment implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Lab lab;
    private final static Logger logger = LogManager.getLogger(ResourcesManager.class);

 // 1. Keep the variable static
    public static SessionFactory factory = buildSessionFactory(); 

    // 2. You don't actually need the factory in the constructor anymore
    public ResourcesManager() {
        super();
        lab = new Lab();
    }

    // 3. Keep your build method as is (or make it private)
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    

    public static boolean createSeatsForLab(Lab lab) {
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
       

    public static boolean createEquipment(Equipment equipment) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // This line is the "Magic": 
                // It creates a placeholder Lab using ONLY the ID.
                // It DOES NOT query the database or load the SeatRecords.
                Lab labProxy = session.getReference(Lab.class, equipment.getLab().getLabId());

                if (labProxy != null) {
                    equipment.setLab(labProxy); // Link the equipment to the placeholder
                    session.merge(equipment);    // Save the equipment
                    tx.commit();
                    return true;
                }
                return false;
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                return false;
            }
        }
    }

//    public static boolean updateEquipment(Equipment equipment) {
//        try (Session session = factory.openSession()) {
//            Transaction tx = session.beginTransaction();
//            try {
//                // Get the ID the client provided
//                String labId = equipment.getLab().getLabId();
//                
//                // Create a proxy/placeholder for the Lab (no seats loaded)
//                Lab labProxy = session.getReference(Lab.class, labId);
//                
//                if (labProxy != null) {
//                	if (labProxy)
//                    equipment.setLab(labProxy);
//                    session.merge(equipment); // Updates the record
//                    tx.commit();
//                    return true;
//                }
//                return false;
//            } catch (Exception e) {
//                if (tx != null) tx.rollback();
//                e.printStackTrace();
//                return false;
//            }
//        }
//    }
    
    public static boolean updateEquipment(Equipment equipment) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // 1. Get the ID from the equipment object
                String labId = equipment.getLab().getLabId();
                
                // 2. Use session.get() to hit the DB and check if it exists
                Lab existingLab = session.get(Lab.class, labId);
                
                // 3. Logic check: Does the Lab exist?
                if (existingLab != null) {
                    // Link the managed lab to the equipment
                    equipment.setLab(existingLab);
                    
                    // Save the changes
                    session.merge(equipment); 
                    tx.commit();
                    System.out.println("Equipment updated successfully for Lab: " + labId);
                    return true;
                } else {
                    // Lab doesn't exist in the DB
                    System.err.println("Update Failed: Lab ID " + labId + " does not exist.");
                    return false;
                }
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                e.printStackTrace();
                return false;
            }
        }
    }

    public static void deleteEquipment(Equipment equip) {
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

    public static Equipment readEquipment(String equipId) {
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

	  //edited below
	    // when a lab is created the seats are created and added into the lab
	 // In ResourcesManager.java
	// In ResourcesManager.java
	public static void createLabOnly(Lab lab) {
	    try (Session session = factory.openSession()) {
	        Transaction tx = session.beginTransaction();
	        try {
	            // 1. Sync the index with the Database
	            int nextIndex = getNextAlphaIndex();
	            SeatRecord.alphaIndex = nextIndex;
	
	            // 2. NOW generate the seats using the updated index
	            lab.setSeatDisplay(lab.addSeats(lab)); 
	
	            // 3. Persist everything
	            session.persist(lab); 
	
	            tx.commit();
	            JOptionPane.showMessageDialog(null, "Lab and Seats created successfully!");
	        } catch (Exception e) {
	            if (tx != null) tx.rollback();
	            throw e;
	        }
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
	    }
	} 

//    public static void createLabWithEquipment(Lab lab, List<Equipment> equipList) {// add equipment to lab immediately when
//          
//        Session session = factory.openSession();
//        Transaction tx = null;
//        try {
//            tx=session.beginTransaction();
//            for (Equipment equip : equipList) {
//                lab.getEquipmentList().add(equip);
//            }
//            createSeatsForLab(lab);
//            session.merge(lab);
//           tx.commit();
//            logger.info("Lab created with equipment successfully: " + lab.getLabId());
//        } catch (Exception e) {
//            tx.rollback();
//            JOptionPane.showMessageDialog(null, "Something went wrong adding equipment to the lab: " + e.getMessage(),
//                    "Lab Status", JOptionPane.ERROR_MESSAGE);
//            logger.error("Something went wrong adding with equipment and or lab " + e.getMessage());
//            tx.rollback();
//        }
//    }

    public static void updateLab(Lab lab) {
        try (Session session = factory.openSession()) {
            session.beginTransaction();
            lab.setSeatDisplay(lab.addSeats(lab));
            lab.setTechnicianId(lab.getTechnicianId());
            session.merge(lab);
            session.getTransaction().commit();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Something went wrong updating the lab: " + e.getMessage(),
                    "Lab Status", JOptionPane.ERROR_MESSAGE);
            logger.error("Something went wrong updating the lab " + e.getMessage());
        }
    }

    public static void deleteLab(Lab lab) {
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

    public static Lab readLab(String labId) {
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
    
    public static Lab readLab(String labId, Session session) {
        Lab lab = session.get(Lab.class, labId);
        if (lab != null) {

            return lab;
        } else {
            JOptionPane.showMessageDialog(null, "Lab not found with ID: " + labId, "Lab Status",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return null;
    }
    
    
    //edited below
    public static int getNextAlphaIndex() {//keep track of alpha index
	    try (Session session = factory.openSession()) {
	        // Query the max seatID alphabetically
	        String hql = "SELECT MAX(s.seatID) FROM SeatRecord s";
	        String maxId = session.createQuery(hql, String.class).uniqueResult();
	
	        if (maxId == null || maxId.isEmpty()) {
	            return 0; // Table is empty, start at 'A'
	        }
	
	        // Get the first character (e.g., 'A' from "A12")
	        char lastLetter = maxId.toUpperCase().charAt(0);
	        
	        // Convert letter to index (A=0, B=1, etc.)
	        int index = lastLetter - 'A';
	        
	        // Return the NEXT letter index (wrap around at 25 if necessary)
	        return (index + 1) % 26;
	    } catch (Exception e) {
	        logger.error("Error fetching alpha index: " + e.getMessage());
	        return 0; 
	    }
    }
    
    public List<Equipment> getAllEquipment() {
	    try (Session session = factory.openSession()) {
	        return session.createQuery("FROM Equipment", Equipment.class).list();
	    } catch (Exception e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Error in GETTING ALL LABS\n" + e.getMessage()); 
	        return null;
	    }
	}
	
//	public static List<Lab> getAllLabs() {
//	    try (Session session = factory.openSession()) {
//	        return session.createQuery("FROM Lab", Lab.class).list();
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        JOptionPane.showMessageDialog(null, "Error in GETTING ALL LABS\n" +  e.getMessage());
//	        return null;
//	    }
//	}
//	
//	public void loadAllLabsWithEquipment() {
//
//	    List<Lab> labs = getAllLabs(); // already created
//
//	    for (Lab lab : labs) {
//	        String labId = lab.getLabId();
//
//	        List<Equipment> equipmentList = getEquipmentByLabId(labId);
//
//	        // 🔥 For now (debug)
//	        System.out.println("Lab: " + lab.getLabName());
//
//	        for (Equipment e : equipmentList) {
//	            System.out.println("  Equipment: " + e.getEquipName());
//	        }
//	    }
//	}
    //update seat status
	
//	public List<Equipment> getEquipmentByLabId(String labId) {
//	    try (Session session = factory.openSession()) {
//
//	        return session.createQuery(
//	            "SELECT e FROM Lab l JOIN l.equipmentList e WHERE l.labId = :labId",
//	            Equipment.class
//	        )
//	        .setParameter("labId", labId)
//	        .list();
//
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return new ArrayList<>();
//	    }
//	}


//    public static void initializeResourceData() {
//        try (Session session = factory.openSession()) {
//           
//            Transaction tx = session.beginTransaction();
//
//            // Create 10 labs
//            String[] labNames = { "Chemistry Lab", "Physics Lab", "Biology Lab", "Computer Lab", "Engineering Lab",
//                    "Math Lab", "History Lab", "Art Lab", "Music Lab", "Sports Lab" };
//            String[] locations = { "Building A", "Building B", "Building C", "Building D", "Building E",
//                    "Building F", "Building G", "Building H", "Building I", "Building J" };
//            int[] capacities = { 20, 25, 15, 25, 18, 12, 10, 22, 14, 23 };
//
//            for (int i = 0; i < 10; i++) {
//                String labId = String.format("L%02d", i + 1);
//                Lab lab = new Lab(labId, labNames[i], locations[i], capacities[i], null);
//                SeatRecord.alphaIndex = i % 26;
//                lab.setSeatDisplay(lab.addSeats(lab));
//                session.persist(lab);
//
//                // Create different types of equipment for each lab
//                String[] equipTypes = { "Microscope", "Computer", "Beaker", "Oscilloscope", "Petri Dish",
//                        "Projector", "Calculator", "Paint Brush", "Piano", "Treadmill" };
//                for (int j = 0; j < 5; j++) { // 5 equipment per lab
//                    String equipId = String.format("E%02d%02d", i + 1, j + 1);
//                    Equipment equip = new Equipment(equipId, equipTypes[j], lab, EquipStatus.AVAILABLE, 10, 10);
//                    session.persist(equip);
//                }
//            }
//
//            tx.commit();
//            JOptionPane.showMessageDialog(null, "Database initialized with 10 labs and equipment",
//                    "Initialization Status", JOptionPane.INFORMATION_MESSAGE);
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null, "Error initializing data: " + e.getMessage(), "Initialization Status",
//                    JOptionPane.ERROR_MESSAGE);
//        }
//    }
    
    public static void initializeResourceData() {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();

            // 1. Defined real names for the 10 technicians
            String[] firstNames = { "Ricardo", "Shelly-Ann", "Usain", "Tessanne", "Christopher", 
                                    "Elaine", "Hansle", "Shericka", "Asafa", "Briana" };
            String[] lastNames = { "Campbell", "Fraser", "Bolt", "Chin", "Martin", 
                                   "Thompson", "Parchment", "Jackson", "Powell", "Williams" };

            String[] labNames = { "Chemistry Lab", "Physics Lab", "Biology Lab", "Computer Lab", "Engineering Lab",
                    "Math Lab", "History Lab", "Art Lab", "Music Lab", "Sports Lab" };
            String[] locations = { "Building A", "Building B", "Building C", "Building D", "Building E",
                    "Building F", "Building G", "Building H", "Building I", "Building J" };
            int[] capacities = { 20, 25, 15, 25, 18, 12, 10, 22, 14, 23 };

            for (int i = 0; i < 10; i++) {
                // 2. Create the Technician using the User class and real names
                String techId = String.format("T%02d", i + 1);
                String email = firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + "@utech.edu.jm";
                
                // Your User class constructor: (userID, firstName, lastName, email, password, role)
                User tech = new User(
                    techId, 
                    firstNames[i], 
                    lastNames[i], 
                    email, 
                    "SecurePass" + (i + 1), 
                    model.Enums.Roles.TECHNICIAN
                );
                
                session.persist(tech); 
                session.flush();
                // 3. Create Lab and link the techId
                String labId = String.format("L%02d", i + 1);
                Lab lab = new Lab(labId, labNames[i], locations[i], capacities[i]);
                lab.setTechnicianId(techId); // Link the technician to the lab

                // 4. Handle eat uniqueness by including Lab ID in the seat name
                SeatRecord.alphaIndex = i % 26; 
                
                session.persist(lab);

                // 5. Create Equipment
                String[] equipTypes = { "Microscope", "Computer", "Beaker", "Oscilloscope", "Petri Dish",
                        "Projector", "Calculator", "Paint Brush", "Piano", "Treadmill" };
                for (int j = 0; j < 5; j++) {
                    String equipId = String.format("E%02d%02d", i + 1, j + 1);
                    Equipment equip = new Equipment(equipId, equipTypes[j], lab, EquipStatus.AVAILABLE, 10, 10);
                    session.persist(equip);
                }
            }

            tx.commit();
            JOptionPane.showMessageDialog(null, "Database initialized with 10 Labs, 10 Technicians, and Equipment",
                    "Initialization Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error initializing data: " + e.getMessage(), "Initialization Status",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void truncateDatabase() {
    	try (Session session = factory.openSession()) {
        Transaction tx = session.beginTransaction();
        
        try {
            // 1. Disable foreign key checks so we can wipe tables in any order
            session.createNativeMutationQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

            // 2. Clear all tables involved in your system
            // Use the actual table names as defined in your @Table annotations or DB
            session.createNativeMutationQuery("TRUNCATE TABLE reservation_equipment").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE reservations").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE equipment").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE seatsRecords").executeUpdate();
            session.createNativeMutationQuery("TRUNCATE TABLE labs").executeUpdate();

            // 3. Re-enable foreign key checks
            session.createNativeMutationQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            
            tx.commit();
            
            JOptionPane.showMessageDialog(null, "Database truncated successfully", 
                    "Database Status", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            JOptionPane.showMessageDialog(null, 
                "Something went wrong truncating the database: " + e.getMessage(),
                "Database Status", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        ResourcesManager manager = new ResourcesManager();
//        truncateDatabase();
        initializeResourceData();
    }

}
