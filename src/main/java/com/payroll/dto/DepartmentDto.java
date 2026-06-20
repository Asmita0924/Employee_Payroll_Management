package com.payroll.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentDto {
    private Integer id;

    @NotBlank(message = "Department name is required")
    private String name;

    private Integer managerId;
    private String managerName;

    public DepartmentDto() {}

    public DepartmentDto(Integer id, String name, Integer managerId, String managerName) {
        this.id = id;
        this.name = name;
        this.managerId = managerId;
        this.managerName = managerName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}
