package com.payroll.service;

import com.payroll.dao.PayrollJdbcDao;
import com.payroll.dto.PayrollDto;
import com.payroll.dto.SalarySlipDto;
import com.payroll.entity.Payroll;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.PayrollRepository;
import com.payroll.util.PdfGeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class PayrollServiceImpl implements PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollServiceImpl.class);

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollJdbcDao payrollJdbcDao;

    public PayrollServiceImpl(PayrollRepository payrollRepository, EmployeeRepository employeeRepository, PayrollJdbcDao payrollJdbcDao) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.payrollJdbcDao = payrollJdbcDao;
    }

    @Override
    @Transactional
    public void generatePayrollForEmployee(Integer employeeId, String month) {
        log.info("Generating payroll using stored procedure for employee ID: " + employeeId + ", Month: " + month);
        try {
            payrollJdbcDao.callGenerateMonthlySalary(employeeId, month);
        } catch (SQLException e) {
            log.error("Failed to generate payroll using stored procedure", e);
            throw new RuntimeException("Error executing salary generation procedure: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void generatePayrollBatch(List<Integer> employeeIds, String month) {
        log.info("Generating payroll in batch for: " + employeeIds.size() + " employees, Month: " + month);
        try {
            // Disables auto-commit, runs execution, uses savepoints, commits/rolls back manual transaction
            payrollJdbcDao.generateMonthlyPayrollBatch(employeeIds, month);
        } catch (SQLException e) {
            log.error("Failed to process payroll batch", e);
            throw new RuntimeException("Error processing transactional batch payroll: " + e.getMessage(), e);
        }
    }

    @Override
    public PayrollDto getPayrollById(Integer salaryId) {
        Payroll payroll = payrollRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with ID: " + salaryId));
        return mapToDto(payroll);
    }

    @Override
    public PayrollDto getPayrollByEmployeeAndMonth(Integer employeeId, String month) {
        Payroll payroll = payrollRepository.findByEmployeeIdAndSalaryMonth(employeeId, month)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found for Employee: " + employeeId + ", Month: " + month));
        return mapToDto(payroll);
    }

    @Override
    public Page<PayrollDto> getPayrollsByMonth(String month, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return payrollRepository.findBySalaryMonth(month, pageable).map(this::mapToDto);
    }

    @Override
    public Page<PayrollDto> getEmployeePayrollHistory(Integer employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("salaryMonth").descending());
        return payrollRepository.findByEmployeeId(employeeId, pageable).map(this::mapToDto);
    }

    @Override
    public byte[] exportSalarySlipPdf(Integer salaryId) {
        Payroll payroll = payrollRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found with ID: " + salaryId));
        
        SalarySlipDto slip = new SalarySlipDto(
                payroll.getEmployee().getName(),
                payroll.getEmployee().getEmail(),
                payroll.getEmployee().getDepartment().getName(),
                payroll.getEmployee().getDesignation(),
                payroll.getEmployee().getJoiningDate(),
                payroll.getSalaryMonth(),
                payroll.getBasicSalary(),
                payroll.getHra(),
                payroll.getDa(),
                payroll.getBonus(),
                payroll.getDeduction(),
                payroll.getTax(),
                payroll.getNetSalary()
        );

        return PdfGeneratorUtil.generateSalarySlipPdf(slip);
    }

    @Override
    public List<Map<String, Object>> getHighestSalariesAnalytics() {
        try {
            return payrollJdbcDao.getHighestSalariesRanked();
        } catch (SQLException e) {
            throw new RuntimeException("Analytics query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getOrgHierarchyAnalytics() {
        try {
            return payrollJdbcDao.getEmployeeHierarchy();
        } catch (SQLException e) {
            throw new RuntimeException("Analytics query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getSalaryGrowthAnalytics() {
        try {
            return payrollJdbcDao.getSalaryGrowthHistory();
        } catch (SQLException e) {
            throw new RuntimeException("Analytics query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getPayrollSummaryAnalytics() {
        try {
            return payrollJdbcDao.getPayrollSummaryReport();
        } catch (SQLException e) {
            throw new RuntimeException("Analytics query failed: " + e.getMessage(), e);
        }
    }

    private PayrollDto mapToDto(Payroll p) {
        return new PayrollDto(
                p.getId(),
                p.getEmployee().getId(),
                p.getEmployee().getName(),
                p.getBasicSalary(),
                p.getHra(),
                p.getDa(),
                p.getBonus(),
                p.getDeduction(),
                p.getTax(),
                p.getNetSalary(),
                p.getSalaryMonth()
        );
    }
}
