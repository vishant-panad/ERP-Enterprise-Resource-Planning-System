-- Database: erp_db
CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;

-- Table: students
-- Profile data for students, linked to auth_db via user_id
CREATE TABLE IF NOT EXISTS students (
    user_id INT PRIMARY KEY COMMENT 'Foreign Key to users_auth.user_id',
    roll_no VARCHAR(20) NOT NULL UNIQUE,
    program VARCHAR(100) NOT NULL,
    year INT NOT NULL
    -- NOTE: Actual FK constraint to a different DB (auth_db) is not supported 
    -- by standard MySQL InnoDB foreign keys directly without specific config.
    -- We assume logical consistency is handled by the app Service layer.
);

-- Table: instructors
-- Profile data for instructors
CREATE TABLE IF NOT EXISTS instructors (
    user_id INT PRIMARY KEY COMMENT 'Foreign Key to users_auth.user_id',
    department VARCHAR(100) NOT NULL
);

-- Table: courses
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL
);

-- Table: sections
CREATE TABLE IF NOT EXISTS sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    instructor_id INT NULL COMMENT 'Can be NULL if instructor not yet assigned',
    day_time VARCHAR(50) NOT NULL COMMENT 'e.g., Mon/Wed 10:00-11:30',
    room VARCHAR(50) NOT NULL,
    capacity INT NOT NULL CHECK (capacity >= 0),
    semester VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id),
    UNIQUE KEY uk_section_time (course_id, day_time, room, semester, year)
);

-- Table: enrollments
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL COMMENT 'Foreign Key to students.user_id',
    section_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'REGISTERED' NOT NULL COMMENT 'e.g., REGISTERED, DROPPED, COMPLETED',
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(user_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id),
    -- Prevents duplicate enrollments (same student in the same section) 
    UNIQUE KEY uk_duplicate_enrollment (student_id, section_id)
);

-- Table: grades
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    component VARCHAR(50) NOT NULL COMMENT 'e.g., Quiz 1, Midterm, End-Sem',
    score DOUBLE NOT NULL CHECK (score >= 0.0),
    final_grade VARCHAR(5) NULL COMMENT 'Letter grade (e.g., A, B+, F)',
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

-- Table: settings
-- Global application settings (Maintenance Mode, etc.)
CREATE TABLE IF NOT EXISTS settings (
    `key` VARCHAR(50) PRIMARY KEY,
    `value` VARCHAR(255) NOT NULL
);