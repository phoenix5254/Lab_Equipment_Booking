package model.users;

import model.users.Enums.Roles;

public class Student extends User {
    private User student;
    public Student(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password,Roles.STUDENT);
    }
    public User getStudent(){ return student; }    
}