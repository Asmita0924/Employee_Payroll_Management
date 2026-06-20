package com.payroll.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface PayrollJdbcDao {

    // Transactional & Batch processing
    void generateMonthlyPayrollBatch(List<Integer> employeeIds, String month) throws SQLException;

    // Stored Procedure calls
    void callGenerateMonthlySalary(int employeeId, String month) throws SQLException;
    void callApproveLeave(int leaveId, String status) throws SQLException;
    void callUpdateEmployeeSalary(int employeeId, double newSalary) throws SQLException;

    // Advanced SQL Analytics (Window functions, CTEs, complex joins)
    List<Map<String, Object>> getHighestSalariesRanked() throws SQLException;
    List<Map<String, Object>> getEmployeeHierarchy() throws SQLException;
    List<Map<String, Object>> getSalaryGrowthHistory() throws SQLException;
    List<Map<String, Object>> getPayrollSummaryReport() throws SQLException;
}
