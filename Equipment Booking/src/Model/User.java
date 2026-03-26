package Model;
import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    protected String password;
    protected byte[] salt;
    private Roles role;

    public User(String userID, String firstName, String lastName, String email, String password, Roles role) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.salt = generateSalt();
        encryptPassword();
    }
    public User(String userID, String firstName, String lastName, String email, String password) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.salt = generateSalt();
        encryptPassword();
    }

    private byte[] generateSalt() {
        // Generate a random salt value
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private void encryptPassword() {
        // Hash the password using the salt
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating password hash", e);
        }
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());
        this.password = Base64.getEncoder().encodeToString(hashedPassword);
    }
    private void resetPassword(String newPassword) {
        this.password = newPassword;
        this.salt = generateSalt();
        encryptPassword();
    }
    public boolean verifyPassword(String inputPassword, String storedPassword, byte[] storedSalt) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(storedSalt);
        byte[] hashedPassword = md.digest(inputPassword.getBytes());
        String inputHashedPassword = Base64.getEncoder().encodeToString(hashedPassword);
        return storedPassword.equals(inputHashedPassword);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Error verifying password", e);
    }
}

    public String getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getSalt() {
        return salt;
    }

    public Roles getRole() {
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public void setRole(Roles role) {
        if(role == Roles.ADMIN){
            this.role = role;
        } else {
            throw new UnsupportedOperationException("Only Admin can set role");
        }
    }

    @Override
    public String toString() {
        return "User\n userID:" + userID + "\nfirstName:" + firstName + ", \nlastName:" + lastName + "\nemail:" + email;
    }
    
}