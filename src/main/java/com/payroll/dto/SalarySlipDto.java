package com.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalarySlipDto {
    private String employeeName;
    private String email;
    private String departmentName;
    private String designation;
    private LocalDate joiningDate;
    private String salaryMonth;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal tax;
    private BigDecimal netSalary;

    public SalarySlipDto() {}

    public SalarySlipDto(String employeeName, String email, String departmentName, String designation, LocalDate joiningDate, String salaryMonth, BigDecimal basicSalary, BigDecimal hra, BigDecimal da, BigDecimal bonus, BigDecimal deduction, BigDecimal tax, BigDecimal netSalary) {
        this.employeeName = employeeName;
        this.email = email;
        this.departmentName = departmentName;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.salaryMonth = salaryMonth;
        this.basicSalary = basicSalary;
        this.hra = hra;
        this.da = da;
        this.bonus = bonus;
        this.deduction = deduction;
        this.tax = tax;
        this.netSalary = netSalary;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(String salaryMonth) {
        this.salaryMonth = salaryMonth;
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
}
