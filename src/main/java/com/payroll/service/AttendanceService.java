package com.payroll.service;

import com.payroll.dto.AttendanceDto;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDto logAttendance(AttendanceDto dto);
    Page<AttendanceDto> getEmployeeAttendance(Integer employeeId, int page, int size);
    List<AttendanceDto> getEmployeeAttendanceBetween(Integer employeeId, LocalDate start, LocalDate end);
}
