-- Database Triggers for Employee Payroll Management System

DELIMITER $$

-- Trigger to track changes to employee base salary
DROP TRIGGER IF EXISTS after_employee_salary_update$$
CREATE TRIGGER after_employee_salary_update
AFTER UPDATE ON employees
FOR EACH ROW
BEGIN
    -- Only log audit when the base salary changes
    IF OLD.base_salary <> NEW.base_salary THEN
        INSERT INTO salary_audit (employee_id, old_salary, new_salary, changed_at, changed_by)
        VALUES (
            NEW.employee_id,
            OLD.base_salary,
            NEW.base_salary,
            NOW(),
            USER() -- Captures the active database user performing the change
        );
    END IF;
END$$

DELIMITER ;
