package com.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payrolls", uniqueConstraints = {@UniqueConstraint(columnNames = {"employee_id", "salary_month"})})
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "basic_salary", nullable = false)
    private BigDecimal basicSalary;

    @Column(name = "hra", nullable = false)
    private BigDecimal hra;

    @Column(name = "da", nullable = false)
    private BigDecimal da;

    @Column(name = "bonus", nullable = false)
    private BigDecimal bonus;

    @Column(name = "deduction", nullable = false)
    private BigDecimal deduction;

    @Column(name = "tax", nullable = false)
    private BigDecimal tax;

    @Column(name = "net_salary", insertable = false, updatable = false)
    private BigDecimal netSalary;

    @Column(name = "salary_month", nullable = false)
    private String salaryMonth; // Format: YYYY-MM

    public Payroll() {}

    public Payroll(Integer id, Employee employee, BigDecimal basicSalary, BigDecimal hra, BigDecimal da, BigDecimal bonus, BigDecimal deduction, BigDecimal tax, String salaryMonth) {
        this.id = id;
        this.employee = employee;
        this.basicSalary = basicSalary;
        this.hra = hra;
        this.da = da;
        this.bonus = bonus;
        this.deduction = deduction;
        this.tax = tax;
        this.salaryMonth = salaryMonth;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
