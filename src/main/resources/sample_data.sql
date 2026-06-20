-- Sample Data for Employee Payroll Management System

-- 1. Insert Initial Departments (without managers first to avoid cyclic reference checks)
INSERT INTO departments (department_name, manager_id) VALUES ('Executive', NULL);
INSERT INTO departments (department_name, manager_id) VALUES ('Information Technology', NULL);
INSERT INTO departments (department_name, manager_id) VALUES ('Human Resources', NULL);
INSERT INTO departments (department_name, manager_id) VALUES ('Finance', NULL);

-- 2. Insert Employees
-- Department ID references: 1 = Executive, 2 = IT, 3 = HR, 4 = Finance
INSERT INTO employees (employee_name, email, phone, department_id, designation, joining_date, base_salary, status)
VALUES ('Robert Baratheon', 'robert.ceo@payroll.com', '123-456-7890', 1, 'Chief Executive Officer', '2020-01-15', 15000.00, 'ACTIVE');

INSERT INTO employees (employee_name, email, phone, department_id, designation, joining_date, base_salary, status)
VALUES ('Eddard Stark', 'eddard.hr@payroll.com', '123-456-7891', 3, 'HR Director', '2020-03-10', 9500.00, 'ACTIVE');

INSERT INTO employees (employee_name, email, phone, department_id, designation, joining_date, base_salary, status)
VALUES ('Jon Snow', 'jon.it@payroll.com', '123-456-7892', 2, 'Senior Developer', '2021-06-01', 7500.00, 'ACTIVE');

INSERT INTO employees (employee_name, email, phone, department_id, designation, joining_date, base_salary, status)
VALUES ('Daenerys Targaryen', 'daenerys.finance@payroll.com', '123-456-7893', 4, 'Finance Manager', '2021-08-20', 11000.00, 'ACTIVE');

INSERT INTO employees (employee_name, email, phone, department_id, designation, joining_date, base_salary, status)
VALUES ('Tyrion Lannister', 'tyrion.dev@payroll.com', '123-456-7894', 2, 'Junior Developer', '2022-02-15', 5500.00, 'ACTIVE');

-- 3. Update Departments with manager IDs now that employees exist
UPDATE departments SET manager_id = 1 WHERE department_id = 1; -- CEO manages Executive
UPDATE departments SET manager_id = 3 WHERE department_id = 2; -- Jon Snow manages IT
UPDATE departments SET manager_id = 2 WHERE department_id = 3; -- Eddard Stark manages HR
UPDATE departments SET manager_id = 4 WHERE department_id = 4; -- Daenerys Targaryen manages Finance

-- 4. Insert Users for Authentication (Passwords are BCrypt hashed version of 'password')
-- Hash: $2a$10$gm1/n4fKY/VimoZUcDAnfufbNosWsKQfgt9/7XJcufNKYNMsrATyu
INSERT INTO users (username, password, role, employee_id)
VALUES ('admin', '$2a$10$gm1/n4fKY/VimoZUcDAnfufbNosWsKQfgt9/7XJcufNKYNMsrATyu', 'ADMIN', NULL);

INSERT INTO users (username, password, role, employee_id)
VALUES ('eddard_hr', '$2a$10$gm1/n4fKY/VimoZUcDAnfufbNosWsKQfgt9/7XJcufNKYNMsrATyu', 'HR', 2);

INSERT INTO users (username, password, role, employee_id)
VALUES ('jon_emp', '$2a$10$gm1/n4fKY/VimoZUcDAnfufbNosWsKQfgt9/7XJcufNKYNMsrATyu', 'EMPLOYEE', 3);

INSERT INTO users (username, password, role, employee_id)
VALUES ('tyrion_emp', '$2a$10$gm1/n4fKY/VimoZUcDAnfufbNosWsKQfgt9/7XJcufNKYNMsrATyu', 'EMPLOYEE', 5);

-- 5. Insert Attendance Records (For Jon Snow (ID 3) and Tyrion Lannister (ID 5) in June 2026)
-- Tyrion Lannister
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (5, '2026-06-01', '09:00:00', '18:00:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (5, '2026-06-02', '08:55:00', '17:30:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (5, '2026-06-03', '09:05:00', '18:05:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (5, '2026-06-04', '09:00:00', '18:00:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (5, '2026-06-05', '09:00:00', '17:00:00');
-- Jon Snow
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (3, '2026-06-01', '08:30:00', '17:30:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (3, '2026-06-02', '08:45:00', '18:00:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (3, '2026-06-03', '09:00:00', '18:00:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (3, '2026-06-04', '09:00:00', '18:00:00');
INSERT INTO attendance (employee_id, date, check_in, check_out) VALUES (3, '2026-06-05', '09:00:00', '17:00:00');

-- 6. Insert Leave Records
-- Tyrion Lannister requests SICK leave, approved
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, status)
VALUES (5, 'SICK', '2026-06-08', '2026-06-09', 'APPROVED');

-- Jon Snow requests CASUAL leave, approved
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, status)
VALUES (3, 'CASUAL', '2026-06-15', '2026-06-15', 'APPROVED');

-- Tyrion Lannister requests ANNUAL leave, pending
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, status)
VALUES (5, 'ANNUAL', '2026-06-25', '2026-06-28', 'PENDING');

-- 7. Insert Initial Payroll Records (Pre-generated for May 2026)
-- Robert Baratheon: Basic=15000, HRA=3000, DA=1500, Bonus=1000, Deduction=0, Tax=2340, Net=18160
INSERT INTO payrolls (employee_id, basic_salary, hra, da, bonus, deduction, tax, salary_month)
VALUES (1, 15000.00, 3000.00, 1500.00, 1000.00, 0.00, 2340.00, '2026-05');

-- Jon Snow: Basic=7500, HRA=1500, DA=750, Bonus=200, Deduction=0, Tax=1194, Net=8756
INSERT INTO payrolls (employee_id, basic_salary, hra, da, bonus, deduction, tax, salary_month)
VALUES (3, 7500.00, 1500.00, 750.00, 200.00, 0.00, 1194.00, '2026-05');
