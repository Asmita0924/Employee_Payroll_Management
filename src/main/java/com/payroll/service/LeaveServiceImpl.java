package com.payroll.service;

import com.payroll.dao.PayrollJdbcDao;
import com.payroll.dto.LeaveDto;
import com.payroll.entity.Employee;
import com.payroll.entity.Leave;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.LeaveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveServiceImpl.class);

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollJdbcDao payrollJdbcDao;

    public LeaveServiceImpl(LeaveRepository leaveRepository, EmployeeRepository employeeRepository, PayrollJdbcDao payrollJdbcDao) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.payrollJdbcDao = payrollJdbcDao;
    }

    @Override
    @Transactional
    public LeaveDto requestLeave(LeaveDto dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        Leave leave = new Leave();
        leave.setEmployee(emp);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setStatus("PENDING"); // Force PENDING on creation

        Leave saved = leaveRepository.save(leave);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void approveLeave(Integer leaveId, String status) {
        log.info("Approving leave using Stored Procedure. Leave ID: " + leaveId + ", Status: " + status);
        try {
            payrollJdbcDao.callApproveLeave(leaveId, status);
        } catch (Exception e) {
            log.error("Failed to execute ApproveLeave stored procedure", e);
            throw new RuntimeException("Error during leave approval processing: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<LeaveDto> getEmployeeLeaves(Integer employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        return leaveRepository.findByEmployeeId(employeeId, pageable).map(this::mapToDto);
    }

    @Override
    public List<LeaveDto> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private LeaveDto mapToDto(Leave leave) {
        return new LeaveDto(
                leave.getId(),
                leave.getEmployee().getId(),
                leave.getEmployee().getName(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getStatus()
        );
    }
}
