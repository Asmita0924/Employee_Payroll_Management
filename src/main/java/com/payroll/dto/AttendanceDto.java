package com.payroll.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceDto {
    private Integer id;

    @NotNull(message = "Employee ID is required")
    private Integer employeeId;

    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkIn;

    @NotNull(message = "Check-out time is required")
    private LocalTime checkOut;

    private BigDecimal workingHours;

    public AttendanceDto() {}

    public AttendanceDto(Integer id, Integer employeeId, String employeeName, LocalDate date, LocalTime checkIn, LocalTime checkOut, BigDecimal workingHours) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.workingHours = workingHours;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalTime checkOut) {
        this.checkOut = checkOut;
    }

    public BigDecimal getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(BigDecimal workingHours) {
        this.workingHours = workingHours;
    }
}
