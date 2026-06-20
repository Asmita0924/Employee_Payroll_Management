package com.payroll.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PayrollJdbcDaoImpl implements PayrollJdbcDao {

    private static final Logger log = LoggerFactory.getLogger(PayrollJdbcDaoImpl.class);
    private final ConnectionFactory connectionFactory;

    public PayrollJdbcDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void generateMonthlyPayrollBatch(List<Integer> employeeIds, String month) throws SQLException {
        Connection conn = null;
        PreparedStatement psInsert = null;
        Savepoint savepoint = null;

        try {
            conn = connectionFactory.getConnection();
            
            // DEMONSTRATE ACID: 1. Disable AutoCommit
            conn.setAutoCommit(false);
            log.debug("Auto-commit disabled. Initiating payroll generation transaction.");

            // DEMONSTRATE ACID: 2. Set Savepoint for partial rollback capability
            savepoint = conn.setSavepoint("PayrollGenerationSavepoint");
            log.debug("Savepoint 'PayrollGenerationSavepoint' set.");

            // SQL to insert payroll
            String sqlInsert = "INSERT INTO payrolls (employee_id, basic_salary, hra, da, bonus, deduction, tax, salary_month) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            psInsert = conn.prepareStatement(sqlInsert);

            for (Integer empId : employeeIds) {
                // Fetch base salary and verify active status
                BigDecimal baseSalary = BigDecimal.ZERO;
                String fetchEmpSql = "SELECT base_salary, status FROM employees WHERE employee_id = ?";
                try (PreparedStatement psEmp = conn.prepareStatement(fetchEmpSql)) {
                    psEmp.setInt(1, empId);
                    try (ResultSet rs = psEmp.executeQuery()) {
                        if (rs.next()) {
                            String status = rs.getString("status");
                            if (!"ACTIVE".equals(status)) {
                                log.warn("Skipping inactive employee ID: " + empId);
                                continue; // Skip inactive employees
                            }
                            baseSalary = rs.getBigDecimal("base_salary");
                        } else {
                            throw new SQLException("Employee not found for ID: " + empId);
                        }
                    }
                }

                // Count approved leaves in the target month
                int leaveCount = 0;
                String leaveSql = "SELECT COUNT(*) FROM leaves WHERE employee_id = ? AND status = 'APPROVED' " +
                                  "AND (DATE_FORMAT(start_date, '%Y-%m') = ? OR DATE_FORMAT(end_date, '%Y-%m') = ?)";
                try (PreparedStatement psLeave = conn.prepareStatement(leaveSql)) {
                    psLeave.setInt(1, empId);
                    psLeave.setString(2, month);
                    psLeave.setString(3, month);
                    try (ResultSet rsLeave = psLeave.executeQuery()) {
                        if (rsLeave.next()) {
                            leaveCount = rsLeave.getInt(1);
                        }
                    }
                }

                // Calculate parameters
                BigDecimal hra = baseSalary.multiply(new BigDecimal("0.20"));
                BigDecimal da = baseSalary.multiply(new BigDecimal("0.10"));
                BigDecimal bonus = BigDecimal.ZERO; // Default zero bonus in batch (can be updated via JPA)
                
                // Leaves deduction logic: Base Salary / 30 for every approved leave day beyond 2
                BigDecimal deduction = BigDecimal.ZERO;
                if (leaveCount > 2) {
                    deduction = baseSalary.divide(new BigDecimal("30.0"), 2, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal(leaveCount - 2));
                }

                // Tax logic: 12% of (Base Salary + HRA + DA - Deduction)
                BigDecimal gross = baseSalary.add(hra).add(da).subtract(deduction);
                BigDecimal tax = gross.multiply(new BigDecimal("0.12"));
                if (tax.compareTo(BigDecimal.ZERO) < 0) {
                    tax = BigDecimal.ZERO;
                }

                // ResultSet mapping and binding
                psInsert.setInt(1, empId);
                psInsert.setBigDecimal(2, baseSalary);
                psInsert.setBigDecimal(3, hra);
                psInsert.setBigDecimal(4, da);
                psInsert.setBigDecimal(5, bonus);
                psInsert.setBigDecimal(6, deduction);
                psInsert.setBigDecimal(7, tax);
                psInsert.setString(8, month);

                psInsert.addBatch();
                log.debug("Added batch entry for employee ID: " + empId);
            }

            // Execute Batch
            int[] results = psInsert.executeBatch();
            log.info("Batch execution completed successfully. Row count updated: " + results.length);

            // DEMONSTRATE ACID: 3. Commit Transaction
            conn.commit();
            log.debug("Transaction committed successfully.");

        } catch (Exception e) {
            log.error("Error occurred during payroll batch processing. Rolling back transaction.", e);
            if (conn != null) {
                try {
                    // DEMONSTRATE ACID: 4. Rollback to Savepoint
                    if (savepoint != null) {
                        conn.rollback(savepoint);
                        log.debug("Transaction rolled back to Savepoint.");
                    } else {
                        conn.rollback();
                        log.debug("Transaction rolled back completely.");
                    }
                } catch (SQLException ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            throw new SQLException("Payroll batch generation failed: " + e.getMessage(), e);
        } finally {
            if (psInsert != null) {
                psInsert.close();
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore defaults
                    conn.close();
                } catch (SQLException ex) {
                    log.error("Failed to close connection", ex);
                }
            }
        }
    }

    @Override
    public void callGenerateMonthlySalary(int employeeId, String month) throws SQLException {
        String callSql = "{call GenerateMonthlySalary(?, ?)}";
        try (Connection conn = connectionFactory.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, employeeId);
            cs.setString(2, month);
            cs.execute();
            log.debug("Stored Procedure GenerateMonthlySalary executed for employee: " + employeeId);
        }
    }

    @Override
    public void callApproveLeave(int leaveId, String status) throws SQLException {
        String callSql = "{call ApproveLeave(?, ?)}";
        try (Connection conn = connectionFactory.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, leaveId);
            cs.setString(2, status);
            cs.execute();
            log.debug("Stored Procedure ApproveLeave executed for leave ID: " + leaveId);
        }
    }

    @Override
    public void callUpdateEmployeeSalary(int employeeId, double newSalary) throws SQLException {
        String callSql = "{call UpdateEmployeeSalary(?, ?)}";
        try (Connection conn = connectionFactory.getConnection();
             CallableStatement cs = conn.prepareCall(callSql)) {
            cs.setInt(1, employeeId);
            cs.setDouble(2, newSalary);
            cs.execute();
            log.debug("Stored Procedure UpdateEmployeeSalary executed for employee: " + employeeId);
        }
    }

    @Override
    public List<Map<String, Object>> getHighestSalariesRanked() throws SQLException {
        String sql = "SELECT " +
                     "  e.employee_id, " +
                     "  e.employee_name, " +
                     "  d.department_name, " +
                     "  e.base_salary, " +
                     "  ROW_NUMBER() OVER (ORDER BY e.base_salary DESC) as row_num, " +
                     "  RANK() OVER (ORDER BY e.base_salary DESC) as sal_rank, " +
                     "  DENSE_RANK() OVER (ORDER BY e.base_salary DESC) as sal_dense_rank " +
                     "FROM employees e " +
                     "JOIN departments d ON e.department_id = d.department_id";
        return executeQueryAndMap(sql);
    }

    @Override
    public List<Map<String, Object>> getEmployeeHierarchy() throws SQLException {
        String sql = "WITH RECURSIVE emp_hierarchy AS ( " +
                     "  SELECT employee_id, employee_name, designation, CAST(NULL AS UNSIGNED) as manager_id, 0 as level " +
                     "  FROM employees " +
                     "  WHERE employee_id = 1 " +
                     "  UNION ALL " +
                     "  SELECT e.employee_id, e.employee_name, e.designation, " +
                     "         CAST(CASE WHEN d.manager_id = e.employee_id THEN 1 ELSE d.manager_id END AS UNSIGNED) as manager_id, " +
                     "         eh.level + 1 " +
                     "  FROM employees e " +
                     "  JOIN departments d ON e.department_id = d.department_id " +
                     "  JOIN emp_hierarchy eh ON ( " +
                     "       CASE WHEN d.manager_id = e.employee_id THEN 1 ELSE d.manager_id END " +
                     "  ) = eh.employee_id " +
                     "  WHERE e.employee_id <> 1 " +
                     ") " +
                     "SELECT * FROM emp_hierarchy ORDER BY level, employee_id";
        return executeQueryAndMap(sql);
    }

    @Override
    public List<Map<String, Object>> getSalaryGrowthHistory() throws SQLException {
        String sql = "SELECT " +
                     "  employee_id, " +
                     "  salary_month, " +
                     "  net_salary, " +
                     "  LAG(net_salary, 1, 0.00) OVER (PARTITION BY employee_id ORDER BY salary_month) as prev_month_salary, " +
                     "  LEAD(net_salary, 1, 0.00) OVER (PARTITION BY employee_id ORDER BY salary_month) as next_month_salary " +
                     "FROM payrolls";
        return executeQueryAndMap(sql);
    }

    @Override
    public List<Map<String, Object>> getPayrollSummaryReport() throws SQLException {
        String sql = "WITH department_payroll AS ( " +
                     "  SELECT " +
                     "    e.department_id, " +
                     "    COUNT(p.salary_id) as total_processed, " +
                     "    SUM(p.net_salary) as total_payout, " +
                     "    AVG(p.net_salary) as avg_payout " +
                     "  FROM payrolls p " +
                     "  JOIN employees e ON p.employee_id = e.employee_id " +
                     "  GROUP BY e.department_id " +
                     ") " +
                     "SELECT " +
                     "  d.department_name, " +
                     "  COALESCE(dp.total_processed, 0) as total_processed, " +
                     "  COALESCE(dp.total_payout, 0.00) as total_payout, " +
                     "  COALESCE(dp.avg_payout, 0.00) as avg_payout " +
                     "FROM departments d " +
                     "LEFT JOIN department_payroll dp ON d.department_id = dp.department_id";
        return executeQueryAndMap(sql);
    }

    private List<Map<String, Object>> executeQueryAndMap(String sql) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    map.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(map);
            }
        }
        return list;
    }
}
