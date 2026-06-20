-- Stored Procedures for Employee Payroll Management System

DELIMITER $$

-- 1. GenerateMonthlySalary Stored Procedure
DROP PROCEDURE IF EXISTS GenerateMonthlySalary$$
CREATE PROCEDURE GenerateMonthlySalary(
    IN in_employee_id INT,
    IN in_month VARCHAR(7)
)
BEGIN
    DECLARE v_base_salary DECIMAL(10,2);
    DECLARE v_hra DECIMAL(10,2);
    DECLARE v_da DECIMAL(10,2);
    DECLARE v_bonus DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_deduction DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_tax DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_leave_count INT DEFAULT 0;
    DECLARE v_emp_exists INT DEFAULT 0;
    DECLARE v_payroll_exists INT DEFAULT 0;
    
    -- Check if Employee Exists
    SELECT COUNT(*) INTO v_emp_exists FROM employees WHERE employee_id = in_employee_id;
    
    IF v_emp_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Employee does not exist.';
    END IF;
    
    -- Check if payroll already exists for the month
    SELECT COUNT(*) INTO v_payroll_exists FROM payrolls WHERE employee_id = in_employee_id AND salary_month = in_month;
    
    IF v_payroll_exists > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Payroll record already exists for this employee for the specified month.';
    END IF;
    
    -- Fetch Base Salary
    SELECT base_salary INTO v_base_salary FROM employees WHERE employee_id = in_employee_id;
    
    -- Calculate HRA (20% of base salary) and DA (10% of base salary)
    SET v_hra = v_base_salary * 0.20;
    SET v_da = v_base_salary * 0.10;
    
    -- Count approved leaves for the given month
    SELECT COUNT(*) INTO v_leave_count 
    FROM leaves 
    WHERE employee_id = in_employee_id 
      AND status = 'APPROVED' 
      AND (DATE_FORMAT(start_date, '%Y-%m') = in_month OR DATE_FORMAT(end_date, '%Y-%m') = in_month);
      
    -- Deductions logic: More than 2 approved leaves in a month results in deduction of (Base Salary / 30) per extra day.
    IF v_leave_count > 2 THEN
        SET v_deduction = (v_leave_count - 2) * (v_base_salary / 30.0);
    END IF;
    
    -- Calculate Tax: 12% of (Base Salary + HRA + DA - Deduction)
    SET v_tax = (v_base_salary + v_hra + v_da - v_deduction) * 0.12;
    IF v_tax < 0 THEN
        SET v_tax = 0;
    END IF;
    
    -- Insert Payroll Record
    INSERT INTO payrolls (employee_id, basic_salary, hra, da, bonus, deduction, tax, salary_month)
    VALUES (in_employee_id, v_base_salary, v_hra, v_da, v_bonus, v_deduction, v_tax, in_month);

END$$

-- 2. ApproveLeave Stored Procedure
DROP PROCEDURE IF EXISTS ApproveLeave$$
CREATE PROCEDURE ApproveLeave(
    IN in_leave_id INT,
    IN in_status VARCHAR(20)
)
BEGIN
    DECLARE v_leave_exists INT DEFAULT 0;
    
    -- Check if status is valid
    IF in_status NOT IN ('APPROVED', 'REJECTED', 'PENDING') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid leave status. Must be APPROVED, REJECTED, or PENDING.';
    END IF;
    
    SELECT COUNT(*) INTO v_leave_exists FROM leaves WHERE leave_id = in_leave_id;
    
    IF v_leave_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Leave request not found.';
    END IF;
    
    -- Update leave status
    UPDATE leaves 
    SET status = in_status 
    WHERE leave_id = in_leave_id;
END$$

-- 3. UpdateEmployeeSalary Stored Procedure
DROP PROCEDURE IF EXISTS UpdateEmployeeSalary$$
CREATE PROCEDURE UpdateEmployeeSalary(
    IN in_employee_id INT,
    IN in_new_salary DECIMAL(10,2)
)
BEGIN
    DECLARE v_emp_exists INT DEFAULT 0;
    
    IF in_new_salary < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Salary cannot be negative.';
    END IF;
    
    SELECT COUNT(*) INTO v_emp_exists FROM employees WHERE employee_id = in_employee_id;
    
    IF v_emp_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Employee not found.';
    END IF;
    
    -- Update salary
    UPDATE employees 
    SET base_salary = in_new_salary 
    WHERE employee_id = in_employee_id;
END$$

DELIMITER ;
