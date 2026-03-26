package Model;

public class Technician extends User {
    private Roles myRole;
    private User technician;
    
    public Technician(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password,myRole=Roles.TECHNICIAN);
    }
    public User getTechnician(){ return technician; } 
}