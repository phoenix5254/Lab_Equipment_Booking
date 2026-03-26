package model;

import java.io.Serializable;
import java.util.Arrays;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userID;
	private String  firstName;
	private String lastName;
	private char[] password;
	private byte [ ] salt;
	private String role;
	
	public String getUserID() {
		return userID;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public char[] getPassword() {
		return password;
	}
	public byte[] getSalt() {
		return salt;
	}
	public String getRole() {
		return role;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setPassword(char[] password) {
		this.password = password;
	}
	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
	public void setRole(String role) {
		this.role = role;
	}
	@Override
	public String toString() {
		return "User [userID=" + userID + ", firstName=" + firstName + ", lastname=" + lastname + ", password="
				+ Arrays.toString(password) + ", salt=" + Arrays.toString(salt) + ", role=" + role + "]";
	}
	
	
}

