package edu.univ.erp.service; // or package edu.univ.erp.auth;

import edu.univ.erp.data.UserAuthDAO;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.util.Result;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserAuthDAO userAuthDAO;

    public AuthService(UserAuthDAO userAuthDAO) {
        this.userAuthDAO = userAuthDAO;
    }

    public AuthService() {
        this.userAuthDAO = new UserAuthDAO();
    }

    
    public Result<UserAuth> login(String username, String plainTextPassword) {
        // 1. Validate Input
        if (username == null || username.trim().isEmpty()) {
            return Result.error("Username cannot be empty.");
        }
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            return Result.error("Password cannot be empty.");
        }

        // 2. Lookup User in Auth DB
        UserAuth user = userAuthDAO.findByUsername(username);

        // DEBUGGING START
        if (user == null) {
            System.out.println("DEBUG CHECK: User '" + username + "' was NOT found in the database.");
            return Result.error("Invalid username or password."); 
        }
        System.out.println("DEBUG CHECK: User found. Stored Hash: " + user.getPasswordHash());
        // DEBUGGING END 

        // 3. Check Account Status
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return Result.error("Account is inactive. Please contact Admin.");
        }

        // 4. Verify Password Hash using BCrypt
        boolean passwordMatch = BCrypt.checkpw(plainTextPassword, user.getPasswordHash());
        
        //  DEBUGGING START 
        System.out.println("DEBUG CHECK: Password match result: " + passwordMatch);
        // DEBUGGING END 

        if (!passwordMatch) {
            return Result.error("Invalid username or password.");
        }

        // 5. Success
        userAuthDAO.updateLastLogin(user.getUserId());
        user.setPasswordHash(null); 
        
        return Result.success("Login successful", user);
    }
}