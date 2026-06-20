package com.payroll.service;

import com.payroll.dto.EmployeeDto;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto dto);
    EmployeeDto updateEmployee(Integer id, EmployeeDto dto);
    EmployeeDto getEmployeeById(Integer id);
    Page<EmployeeDto> searchEmployees(String name, Integer departmentId, int page, int size, String sortBy, String sortDir);
    void deleteEmployee(Integer id);
    void updateBaseSalary(Integer employeeId, double newSalary);
}
