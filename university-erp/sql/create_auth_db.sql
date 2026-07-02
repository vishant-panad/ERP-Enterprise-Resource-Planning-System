-- Database: auth_db
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- Table: users_auth
-- Stores credentials and roles. Passwords must be hashed (BCrypt).
CREATE TABLE IF NOT EXISTS users_auth (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Links to profiles in ERP DB',
    username VARCHAR(50) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,
    password_hash VARCHAR(255) NOT NULL COMMENT 'Secure hash (e.g., bcrypt), never plaintext',
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    last_login TIMESTAMP NULL
);
