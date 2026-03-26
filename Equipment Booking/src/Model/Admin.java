package Model;

public class Admin extends User{
    private Roles myRole;
    private User admin;
    public Admin(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password,myRole=Roles.ADMIN);
    }
    public User getAdmin(){ return admin; } 
    public void review(){}
    public void approve(){}
    public void reject(){}
    public void cancel(){}
    public void assignTechnician(){}
    public void makeReservation(){}
    public void addEquipment(){}
    public void removeEquipment(){}
    public void addResourcesToLab(){}
}