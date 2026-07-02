package edu.univ.erp.access;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;

public class AccessChecker {

    /**
     * Checks if the currently logged-in user is allowed to perform a specific action.
     * * @param targetUserId The ID of the user whose data is being accessed (e.g., studentId).
     * Pass 0 if the action is global (like "BROWSE_CATALOG").
     * @param action       The action identifier string.
     */
    public static boolean isAllowed(int targetUserId, String action) {
        if (!SessionManager.isLoggedIn()) {
            return false; 
        }

        UserAuth currentUser = SessionManager.getCurrentUser();
        String role = currentUser.getRole();
        int currentId = currentUser.getUserId();

        // 1. ADMIN Rule: Admins can do anything [cite: 14]
        if ("ADMIN".equals(role)) {
            return true;
        }

        // 2. INSTRUCTOR Rules [cite: 15]
        if ("INSTRUCTOR".equals(role)) {
            // Instructors can only view/manage their own sections and grades
            // (Specific section ownership checks usually happen in the Service layer, 
            // but global permission checks happen here).
            switch (action) {
                case "ENTER_GRADES":
                case "VIEW_MY_SECTIONS":
                case "CALCULATE_GPA":
                case "BROWSE_CATALOG": // Instructors can typically see the catalog
                    return true;
                default:
                    return false;
            }
        }

        // 3. STUDENT Rules [cite: 16]
        if ("STUDENT".equals(role)) {
            switch (action) {
                case "BROWSE_CATALOG":
                    return true; // Public data for students
                
                case "REGISTER_SECTION":
                case "DROP_SECTION":
                case "VIEW_TIMETABLE":
                case "VIEW_GRADES":
                case "DOWNLOAD_TRANSCRIPT":
                    // Crucial Check: Students can only act on THEIR OWN userId [cite: 155]
                    return targetUserId == currentId;
                    
                default:
                    return false;
            }
        }

        return false;
    }
}