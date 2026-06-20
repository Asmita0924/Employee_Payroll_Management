package com.payroll.service;

import com.payroll.dto.AttendanceDto;
import com.payroll.entity.Attendance;
import com.payroll.entity.Employee;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.AttendanceRepository;
import com.payroll.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public AttendanceDto logAttendance(AttendanceDto dto) {
        Employee emp = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));

        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(dto.getEmployeeId(), dto.getDate());
        
        Attendance attendance;
        if (existing.isPresent()) {
            attendance = existing.get();
            attendance.setCheckIn(dto.getCheckIn());
            attendance.setCheckOut(dto.getCheckOut());
        } else {
            attendance = new Attendance();
            attendance.setEmployee(emp);
            attendance.setDate(dto.getDate());
            attendance.setCheckIn(dto.getCheckIn());
            attendance.setCheckOut(dto.getCheckOut());
        }

        Attendance saved = attendanceRepository.save(attendance);
        // Refresh database state to pull generated working_hours if needed,
        // or let Hibernate/JPA fetch. We'll map from the saved entity.
        return mapToDto(saved);
    }

    @Override
    public Page<AttendanceDto> getEmployeeAttendance(Integer employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return attendanceRepository.findByEmployeeId(employeeId, pageable).map(this::mapToDto);
    }

    @Override
    public List<AttendanceDto> getEmployeeAttendanceBetween(Integer employeeId, LocalDate start, LocalDate end) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AttendanceDto mapToDto(Attendance att) {
        return new AttendanceDto(
                att.getId(),
                att.getEmployee().getId(),
                att.getEmployee().getName(),
                att.getDate(),
                att.getCheckIn(),
                att.getCheckOut(),
                att.getWorkingHours()
        );
    }
}
