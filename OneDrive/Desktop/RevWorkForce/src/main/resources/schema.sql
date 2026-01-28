-- Database Schema for Rev Workforce

-- Create Database
DROP DATABASE IF EXISTS revworkforce;
CREATE DATABASE IF NOT EXISTS revworkforce;
USE revworkforce;

-- Employee Table
CREATE TABLE IF NOT EXISTS employee (
    employee_id INT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    role VARCHAR(20), -- EMPLOYEE, MANAGER, ADMIN
    manager_id INT,
    phone VARCHAR(20),
    address VARCHAR(255),
    department VARCHAR(50),
    designation VARCHAR(50),
    joining_date DATE,
    salary DECIMAL(10,2), -- Added Salary
    status VARCHAR(20) DEFAULT 'ACTIVE',
    dob DATE, -- New field
    emergency_contact VARCHAR(255), -- New field
    security_question VARCHAR(255), -- For password recovery
    security_answer VARCHAR(255) -- Hashed answer
);

-- Leave Balance Table
CREATE TABLE IF NOT EXISTS leave_balance (
    employee_id INT PRIMARY KEY,
    cl INT DEFAULT 12, -- Casual Leave
    sl INT DEFAULT 12, -- Sick Leave
    pl INT DEFAULT 12, -- Paid Leave
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Leave Request Table
CREATE TABLE IF NOT EXISTS leave_request (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    leave_type VARCHAR(10),
    start_date DATE,
    end_date DATE,
    reason VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    manager_comment VARCHAR(255),
    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Performance Review Table
CREATE TABLE IF NOT EXISTS performance_review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    year INT,
    self_assessment TEXT,
    achievements TEXT,
    improvement_areas TEXT,
    manager_feedback TEXT,
    rating INT,
    status VARCHAR(20) DEFAULT 'SUBMITTED', -- SUBMITTED, REVIEWED
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Performance Goal Table
CREATE TABLE IF NOT EXISTS performance_goal (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    goal_description VARCHAR(255),
    deadline DATE,
    priority VARCHAR(10), -- HIGH, MEDIUM, LOW
    success_metrics VARCHAR(255),
    progress INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Notification Table
CREATE TABLE IF NOT EXISTS notification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    message VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Company Holiday Table (New)
CREATE TABLE IF NOT EXISTS company_holiday (
    id INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) -- e.g., National, Optional
);

-- Announcement Table (New)
CREATE TABLE IF NOT EXISTS announcement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    posted_date DATE DEFAULT (CURRENT_DATE),
    posted_by INT, -- Admin ID
    FOREIGN KEY (posted_by) REFERENCES employee(employee_id)
);

-- Audit Log Table (New)
CREATE TABLE IF NOT EXISTS audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255),
    performed_by INT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (performed_by) REFERENCES employee(employee_id)
);

-- Insert Dummy Admin (if not exists)
-- INSERT INTO employee (employee_id, name, email, password, role) 
-- VALUES (1, 'Admin User', 'admin@rev.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMIN');
