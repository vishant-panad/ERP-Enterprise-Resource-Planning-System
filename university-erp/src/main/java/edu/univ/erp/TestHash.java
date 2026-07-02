package edu.univ.erp;
import org.mindrot.jbcrypt.BCrypt;

public class TestHash {
    public static void main(String[] args) {
        String password = "password";
        // Generate a fresh hash
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("NEW HASH: " + hash);
    }
}