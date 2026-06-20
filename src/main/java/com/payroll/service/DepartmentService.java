package com.payroll.service;

import com.payroll.dto.DepartmentDto;
import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto dto);
    DepartmentDto getDepartmentById(Integer id);
    List<DepartmentDto> getAllDepartments();
    DepartmentDto updateDepartment(Integer id, DepartmentDto dto);
    void deleteDepartment(Integer id);
}
