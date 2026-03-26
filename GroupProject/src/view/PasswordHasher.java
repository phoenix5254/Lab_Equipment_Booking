package view;

import org.mindrot.jbcrypt.BCrypt;
//https://youtu.be/2UYJcoEq2SI HASHING PASSWORD
public class PasswordHasher {

    // Hash a password
    public static String hashPassword(String plaintextPassword) {
        // Generate a random salt with a default workload (10)
        String salt = BCrypt.gensalt(); 
        // Hash the password with the generated salt
        return BCrypt.hashpw(plaintextPassword, salt);
    }

    // Verify a password
    public static boolean verifyPassword(String plaintextPassword, String storedHash) {
        // Check if the plaintext password matches the stored hash. 
        // The checkpw method extracts the salt and cost factor from the hash itself.
        return BCrypt.checkpw(plaintextPassword, storedHash);
    }

    public static void main(String[] args) {
        String password = "mySecretPassword123";
        String hashed = hashPassword(password);
        System.out.println("Hashed password: " + hashed);

        boolean matched = verifyPassword("mySecretPassword123", hashed);
        System.out.println("Password match: " + matched); // true

        boolean mismatched = verifyPassword("wrongPassword", hashed);
        System.out.println("Wrong password match: " + mismatched); // false
    }
}
