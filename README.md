University ERP System

A Java Swing application for university management, featuring role-based access, a dual-database architecture, and PDF transcript generation.

1. Prerequisites
* Java JDK 17 (or higher)
* MySQL Server 8.0+ (running on localhost:3306)
* Apache Maven (for building the project)

2. Database Setup (Required)
The application uses two databases: `auth_db` (Security) and `erp_db` (Academic Data).

1.  Open MySQL Workbench.
2.  Navigate to the `sql/` folder in this project.
3.  Execute the scripts in this exact order:
    1. `create_auth_db.sql`
    2. `create_erp_db.sql`
    3. `seed_auth.sql` 
    4. `seed_erp.sql` 

How to Run
1.  Open a terminal in the project root folder.
2.  Run the commands:
    mvn clean package

    then

    java -jar target/erp-1.0-SNAPSHOT.jar

Default settings:
* User: `root`
* Password: `Vish1250$$` (Update this file if your MySQL password differs).# AP-project
University ERP
