package edu.univ.erp.auth;

import edu.univ.erp.domain.UserAuth;


public class SessionManager {

    private static UserAuth currentUser;

    // Prevent instantiation
    private SessionManager() {}

   
    public static void login(UserAuth user) {
        currentUser = user;
    }

   
    public static void logout() {
        currentUser = null;
    }

    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    
    public static UserAuth getCurrentUser() {
        return currentUser;
    }
}