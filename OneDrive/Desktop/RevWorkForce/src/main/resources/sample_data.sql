-- Sample Data for RevWorkForce

-- Insert Admin
INSERT INTO employee (employee_id, name, email, password, role, status, joining_date, dob, salary) 
VALUES (1, 'Admin User', 'admin@rev.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMIN', 'ACTIVE', CURRENT_DATE, '1990-01-01', 100000.00);

-- Insert Manager
INSERT INTO employee (employee_id, name, email, password, role, status, joining_date, dob, salary) 
VALUES (2, 'Manager One', 'manager@rev.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'MANAGER', 'ACTIVE', CURRENT_DATE, '1992-05-15', 80000.00);

-- Insert Employee
INSERT INTO employee (employee_id, name, email, password, role, manager_id, status, joining_date, dob, salary) 
VALUES (3, 'Employee One', 'user@rev.com', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'EMPLOYEE', 2, 'ACTIVE', CURRENT_DATE, '1995-10-20', 50000.00);

-- Insert Leave Balances
INSERT INTO leave_balance (employee_id, cl, sl, pl) VALUES (1, 12, 12, 12);
INSERT INTO leave_balance (employee_id, cl, sl, pl) VALUES (2, 12, 12, 12);
INSERT INTO leave_balance (employee_id, cl, sl, pl) VALUES (3, 12, 12, 12);

-- Insert Holidays
INSERT INTO company_holiday (holiday_date, name, type) VALUES ('2024-01-01', 'New Year', 'National');
INSERT INTO company_holiday (holiday_date, name, type) VALUES ('2024-08-15', 'Independence Day', 'National');
INSERT INTO company_holiday (holiday_date, name, type) VALUES ('2024-12-25', 'Christmas', 'National');

-- Insert Announcement
INSERT INTO announcement (message, posted_by) VALUES ('Welcome to the new HRMS system!', 1);
