-- Drop existing tables in reverse order of dependencies to avoid FK errors
DROP TABLE IF EXISTS salary_audit;
DROP TABLE IF EXISTS payrolls;
DROP TABLE IF EXISTS leaves;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;

-- 1. Departments Table
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    manager_id INT NULL
);

-- 2. Employees Table
CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    department_id INT NOT NULL,
    designation VARCHAR(100) NOT NULL,
    joining_date DATE NOT NULL,
    base_salary DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_employee_salary CHECK (base_salary >= 0),
    CONSTRAINT chk_employee_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE RESTRICT
);

-- Add manager foreign key constraint to departments table to link back to employees
ALTER TABLE departments ADD CONSTRAINT fk_department_manager FOREIGN KEY (manager_id) REFERENCES employees(employee_id) ON DELETE SET NULL;

-- 3. Users Table (Authentication)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    employee_id INT UNIQUE NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'HR', 'EMPLOYEE')),
    CONSTRAINT fk_user_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- 4. Attendance Table
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    date DATE NOT NULL,
    check_in TIME NOT NULL,
    check_out TIME NOT NULL,
    working_hours DECIMAL(5,2) GENERATED ALWAYS AS (TIMESTAMPDIFF(MINUTE, CONCAT(date, ' ', check_in), CONCAT(date, ' ', check_out)) / 60.0) STORED,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT uq_employee_date UNIQUE (employee_id, date)
);

-- 5. Leaves Table
CREATE TABLE leaves (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_leave_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_leave_type CHECK (leave_type IN ('SICK', 'CASUAL', 'ANNUAL', 'MATERNITY', 'PATERNITY')),
    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date),
    CONSTRAINT fk_leave_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- 6. Payrolls Table
CREATE TABLE payrolls (
    salary_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    hra DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    da DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    bonus DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    net_salary DECIMAL(10,2) GENERATED ALWAYS AS (basic_salary + hra + da + bonus - deduction - tax) STORED,
    salary_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    CONSTRAINT chk_payroll_salaries CHECK (basic_salary >= 0 AND hra >= 0 AND da >= 0 AND bonus >= 0 AND deduction >= 0 AND tax >= 0),
    CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT uq_employee_month UNIQUE (employee_id, salary_month)
);

-- 7. Salary Audit Table
CREATE TABLE salary_audit (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    old_salary DECIMAL(10,2) NOT NULL,
    new_salary DECIMAL(10,2) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) NOT NULL
);

-- Indexing for high-performance retrieval and search optimization
CREATE INDEX idx_employees_employee_id ON employees(employee_id);
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_payrolls_salary_month ON payrolls(salary_month);
CREATE INDEX idx_attendance_emp_date ON attendance(employee_id, date);
CREATE INDEX idx_leaves_emp_dates ON leaves(employee_id, start_date, end_date);
