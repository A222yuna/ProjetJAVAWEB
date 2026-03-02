-- Appointment Management System - Database Setup Script

-- Create Database
CREATE DATABASE IF NOT EXISTS appointments_db;
USE appointments_db;

-- Create Users Table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role ENUM('PATIENT', 'PSYCHOLOGUE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Psychologue Plans Table
CREATE TABLE psychologue_plans (
    id INT PRIMARY KEY AUTO_INCREMENT,
    psychologue_id INT NOT NULL,
    day_of_week ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    period ENUM('DAY','NIGHT') NOT NULL,
    max_appointments INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (psychologue_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Appointments Table
CREATE TABLE appointments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    plan_id INT NOT NULL,
    status ENUM('SCHEDULED','CANCELLED','COMPLETED') DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES psychologue_plans(id) ON DELETE CASCADE
);

-- Create Indexes for Performance
CREATE INDEX idx_patient_id ON appointments(patient_id);
CREATE INDEX idx_plan_id ON appointments(plan_id);
CREATE INDEX idx_psychologue_id ON psychologue_plans(psychologue_id);
CREATE INDEX idx_username ON users(username);

-- Insert Sample Data
INSERT INTO users (username, password, full_name, email, phone, role) VALUES
('doctor_ahmed', 'pass123', 'Dr. Ahmed Ben Ali', 'ahmed.benali@example.com', '+216 50 123 456', 'PSYCHOLOGUE'),
('doctor_fatima', 'pass123', 'Dr. Fatima Zahra', 'fatima.zahra@example.com', '+216 50 234 567', 'PSYCHOLOGUE'),
('doctor_mohammad', 'pass123', 'Dr. Mohammad Hassan', 'mohammad.hassan@example.com', '+216 50 345 678', 'PSYCHOLOGUE'),
('patient_ali', 'pass123', 'Ali Mohamed', 'ali.mohamed@example.com', '+216 50 456 789', 'PATIENT'),
('patient_leila', 'pass123', 'Leila Ahmed', 'leila.ahmed@example.com', '+216 50 567 890', 'PATIENT'),
('patient_karim', 'pass123', 'Karim Hassan', 'karim.hassan@example.com', '+216 50 678 901', 'PATIENT'),
('patient_noor', 'pass123', 'Noor Khalid', 'noor.khalid@example.com', '+216 50 789 012', 'PATIENT');

-- Insert Sample Psychologue Schedules
INSERT INTO psychologue_plans (psychologue_id, day_of_week, period, max_appointments) VALUES
(1, 'MONDAY', 'DAY', 5),
(1, 'MONDAY', 'NIGHT', 4),
(1, 'WEDNESDAY', 'DAY', 5),
(1, 'FRIDAY', 'NIGHT', 4),
(2, 'TUESDAY', 'DAY', 6),
(2, 'TUESDAY', 'NIGHT', 5),
(2, 'THURSDAY', 'DAY', 6),
(2, 'SATURDAY', 'NIGHT', 4),
(3, 'MONDAY', 'NIGHT', 5),
(3, 'WEDNESDAY', 'DAY', 6),
(3, 'FRIDAY', 'DAY', 5),
(3, 'SUNDAY', 'NIGHT', 4);

-- Insert Sample Appointments
INSERT INTO appointments (patient_id, plan_id, status) VALUES
(4, 1, 'SCHEDULED'),
(4, 2, 'COMPLETED'),
(5, 3, 'SCHEDULED'),
(6, 5, 'SCHEDULED'),
(7, 6, 'CANCELLED'),
(4, 7, 'SCHEDULED'),
(5, 10, 'COMPLETED');

-- Verify Installation
SELECT 'Users Table:' AS status;
SELECT COUNT(*) as total_users, role FROM users GROUP BY role;

SELECT 'Psychologue Plans Table:' AS status;
SELECT COUNT(*) as total_plans FROM psychologue_plans;

SELECT 'Appointments Table:' AS status;
SELECT COUNT(*) as total_appointments, status FROM appointments GROUP BY status;