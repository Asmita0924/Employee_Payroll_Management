package com.payroll.service;

import com.payroll.dto.PayrollDto;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface PayrollService {
    void generatePayrollForEmployee(Integer employeeId, String month);
    void generatePayrollBatch(List<Integer> employeeIds, String month);
    
    PayrollDto getPayrollById(Integer salaryId);
    PayrollDto getPayrollByEmployeeAndMonth(Integer employeeId, String month);
    Page<PayrollDto> getPayrollsByMonth(String month, int page, int size);
    Page<PayrollDto> getEmployeePayrollHistory(Integer employeeId, int page, int size);
    
    byte[] exportSalarySlipPdf(Integer salaryId);

    // Advanced SQL Analytics methods
    List<Map<String, Object>> getHighestSalariesAnalytics();
    List<Map<String, Object>> getOrgHierarchyAnalytics();
    List<Map<String, Object>> getSalaryGrowthAnalytics();
    List<Map<String, Object>> getPayrollSummaryAnalytics();
}
