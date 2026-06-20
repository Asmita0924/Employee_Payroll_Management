package com.payroll.dto;

import java.math.BigDecimal;

public class PayrollDto {
    private Integer id;
    private Integer employeeId;
    private String employeeName;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal tax;
    private BigDecimal netSalary;
    private String salaryMonth;

    public PayrollDto() {}

    public PayrollDto(Integer id, Integer employeeId, String employeeName, BigDecimal basicSalary, BigDecimal hra, BigDecimal da, BigDecimal bonus, BigDecimal deduction, BigDecimal tax, BigDecimal netSalary, String salaryMonth) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.basicSalary = basicSalary;
        this.hra = hra;
        this.da = da;
        this.bonus = bonus;
        this.deduction = deduction;
        this.tax = tax;
        this.netSalary = netSalary;
        this.salaryMonth = salaryMonth;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getHra() {
        return hra;
    }

    public void setHra(BigDecimal hra) {
        this.hra = hra;
    }

    public BigDecimal getDa() {
        return da;
    }

    public void setDa(BigDecimal da) {
        this.da = da;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getDeduction() {
        return deduction;
    }

    public void setDeduction(BigDecimal deduction) {
        this.deduction = deduction;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public String getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(String salaryMonth) {
        this.salaryMonth = salaryMonth;
    }
}
