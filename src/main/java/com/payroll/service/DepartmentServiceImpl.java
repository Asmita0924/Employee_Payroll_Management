package com.payroll.service;

import com.payroll.dto.DepartmentDto;
import com.payroll.entity.Department;
import com.payroll.entity.Employee;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.DepartmentRepository;
import com.payroll.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        Department dept = new Department();
        dept.setName(dto.getName());
        dept.setManagerId(dto.getManagerId());
        Department saved = departmentRepository.save(dept);
        return mapToDto(saved);
    }

    @Override
    public DepartmentDto getDepartmentById(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return mapToDto(dept);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Integer id, DepartmentDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        dept.setName(dto.getName());
        dept.setManagerId(dto.getManagerId());
        Department updated = departmentRepository.save(dept);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        departmentRepository.delete(dept);
    }

    private DepartmentDto mapToDto(Department dept) {
        String managerName = null;
        if (dept.getManagerId() != null) {
            managerName = employeeRepository.findById(dept.getManagerId())
                    .map(Employee::getName)
                    .orElse(null);
        }
        return new DepartmentDto(
                dept.getId(),
                dept.getName(),
                dept.getManagerId(),
                managerName
        );
    }
}
