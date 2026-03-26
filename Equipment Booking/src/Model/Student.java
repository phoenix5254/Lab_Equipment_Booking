package Model;

public class Student extends User {
    private Roles myRole;
    private User student;
    public Student(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password, myRole = Roles.STUDENT);
    }
    public User getStudent(){ return student; }    
}