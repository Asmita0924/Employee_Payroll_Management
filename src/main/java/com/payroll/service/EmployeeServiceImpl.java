package com.payroll.service;

import com.payroll.dao.PayrollJdbcDao;
import com.payroll.dto.EmployeeDto;
import com.payroll.entity.Department;
import com.payroll.entity.Employee;
import com.payroll.entity.User;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.DepartmentRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PayrollJdbcDao payrollJdbcDao;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, 
                               DepartmentRepository departmentRepository,
                               UserRepository userRepository,
                               PayrollJdbcDao payrollJdbcDao,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.payrollJdbcDao = payrollJdbcDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto dto) {
        log.info("Creating new employee: " + dto.getName());
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        Employee emp = new Employee();
        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPhone(dto.getPhone());
        emp.setDepartment(dept);
        emp.setDesignation(dto.getDesignation());
        emp.setJoiningDate(dto.getJoiningDate());
        emp.setBaseSalary(dto.getBaseSalary());
        emp.setStatus(dto.getStatus() == null ? "ACTIVE" : dto.getStatus());

        Employee savedEmp = employeeRepository.save(emp);

        // Auto-create User account for the employee
        String defaultUsername = dto.getEmail().split("@")[0];
        User user = new User();
        user.setUsername(defaultUsername);
        user.setPassword(passwordEncoder.encode("password")); // Default password
        user.setRole("EMPLOYEE");
        user.setEmployee(savedEmp);
        userRepository.save(user);
        log.info("Auto-created user account with username: " + defaultUsername);

        return mapToDto(savedEmp);
    }

    @Override
    @Transactional
    public EmployeeDto updateEmployee(Integer id, EmployeeDto dto) {
        log.info("Updating employee with ID: " + id);
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));

        emp.setName(dto.getName());
        emp.setEmail(dto.getEmail());
        emp.setPhone(dto.getPhone());
        emp.setDepartment(dept);
        emp.setDesignation(dto.getDesignation());
        emp.setJoiningDate(dto.getJoiningDate());
        emp.setBaseSalary(dto.getBaseSalary());
        if (dto.getStatus() != null) {
            emp.setStatus(dto.getStatus());
        }

        Employee updatedEmp = employeeRepository.save(emp);
        return mapToDto(updatedEmp);
    }

    @Override
    public EmployeeDto getEmployeeById(Integer id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return mapToDto(emp);
    }

    @Override
    public Page<EmployeeDto> searchEmployees(String name, Integer departmentId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employeePage = employeeRepository.searchEmployees(name, departmentId, pageable);
        return employeePage.map(this::mapToDto);
    }

    @Override
    @Transactional
    public void deleteEmployee(Integer id) {
        log.info("Deleting employee with ID: " + id);
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        employeeRepository.delete(emp);
    }

    @Override
    @Transactional
    public void updateBaseSalary(Integer employeeId, double newSalary) {
        log.info("Updating base salary using Stored Procedure for employee ID: " + employeeId);
        try {
            payrollJdbcDao.callUpdateEmployeeSalary(employeeId, newSalary);
        } catch (Exception e) {
            log.error("Failed to execute UpdateEmployeeSalary stored procedure", e);
            throw new RuntimeException("Error updating salary: " + e.getMessage(), e);
        }
    }

    private EmployeeDto mapToDto(Employee emp) {
        return new EmployeeDto(
                emp.getId(),
                emp.getName(),
                emp.getEmail(),
                emp.getPhone(),
                emp.getDepartment().getId(),
                emp.getDepartment().getName(),
                emp.getDesignation(),
                emp.getJoiningDate(),
                emp.getBaseSalary(),
                emp.getStatus()
        );
    }
}
