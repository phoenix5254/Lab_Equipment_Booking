package com_java_users;

import com_java_users.Enums.Roles;

public class Technician extends User {
    private User technician;
    
    public Technician(String userID, String firstName, String lastName, String email, String password) {
        super(userID, firstName, lastName, email, password,Roles.TECHNICIAN);

    }
    public User getTechnician(){ return technician; } 
}