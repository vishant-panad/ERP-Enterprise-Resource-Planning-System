package edu.univ.erp.service;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.UserAuthDAO;


public class ServiceFactory {

    // DAOs (Data Access Objects)
    private final UserAuthDAO userAuthDAO = new UserAuthDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final SectionDAO sectionDAO = new SectionDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final GradeDAO gradeDAO = new GradeDAO();

    // SERVICES (Business Logic)
    private final AuthService authService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    private final AdminService adminService;


    public ServiceFactory() {
        // Wire DAOs into Services
        this.authService = new AuthService(userAuthDAO);
        
        // StudentService was already updated in your local files to take DAOs, so we pass them
        this.studentService = new StudentService(sectionDAO, enrollmentDAO); 
        
        this.instructorService = new InstructorService(sectionDAO, enrollmentDAO, gradeDAO);
        
        // AdminService creation
        this.adminService = new AdminService(courseDAO, sectionDAO); 
       
    }

    // --- Public Getters for Services ---

    public AuthService getAuthService() {
        return authService;
    }

    public StudentService getStudentService() {
        return studentService;
    }

    public InstructorService getInstructorService() {
        return instructorService;
    }

    public AdminService getAdminService() {
        return adminService;
    }
}