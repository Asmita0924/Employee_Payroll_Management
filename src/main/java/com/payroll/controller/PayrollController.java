package com.payroll.controller;

import com.payroll.dto.PayrollDto;
import com.payroll.service.PayrollService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generatePayroll(
            @RequestParam Integer employeeId,
            @RequestParam String month) {
        payrollService.generatePayrollForEmployee(employeeId, month);
        return ResponseEntity.ok(Map.of("message", "Payroll generated successfully via stored procedure call for month " + month));
    }

    @PostMapping("/generate-batch")
    public ResponseEntity<Map<String, String>> generatePayrollBatch(
            @RequestBody List<Integer> employeeIds,
            @RequestParam String month) {
        payrollService.generatePayrollBatch(employeeIds, month);
        return ResponseEntity.ok(Map.of("message", "Batch payroll generation completed successfully. Transaction executed."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollDto> getPayrollById(@PathVariable Integer id) {
        PayrollDto dto = payrollService.getPayrollById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/employee/{employeeId}/month")
    public ResponseEntity<PayrollDto> getPayrollByEmployeeAndMonth(
            @PathVariable Integer employeeId,
            @RequestParam String month) {
        PayrollDto dto = payrollService.getPayrollByEmployeeAndMonth(employeeId, month);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<PayrollDto>> getEmployeePayrollHistory(
            @PathVariable Integer employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PayrollDto> history = payrollService.getEmployeePayrollHistory(employeeId, page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping
    public ResponseEntity<Page<PayrollDto>> getPayrollsByMonth(
            @RequestParam String month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PayrollDto> history = payrollService.getPayrollsByMonth(month, page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSalarySlip(@PathVariable Integer id) {
        byte[] pdfBytes = payrollService.exportSalarySlipPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "SalarySlip_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // Advanced SQL Analytics endpoints
    @GetMapping("/analytics/rank")
    public ResponseEntity<List<Map<String, Object>>> getHighestSalaries() {
        return ResponseEntity.ok(payrollService.getHighestSalariesAnalytics());
    }

    @GetMapping("/analytics/hierarchy")
    public ResponseEntity<List<Map<String, Object>>> getOrgHierarchy() {
        return ResponseEntity.ok(payrollService.getOrgHierarchyAnalytics());
    }

    @GetMapping("/analytics/growth")
    public ResponseEntity<List<Map<String, Object>>> getSalaryGrowth() {
        return ResponseEntity.ok(payrollService.getSalaryGrowthAnalytics());
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<List<Map<String, Object>>> getPayrollSummary() {
        return ResponseEntity.ok(payrollService.getPayrollSummaryAnalytics());
    }
}
