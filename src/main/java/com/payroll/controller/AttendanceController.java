package com.payroll.controller;

import com.payroll.dto.AttendanceDto;
import com.payroll.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<AttendanceDto> logAttendance(@Valid @RequestBody AttendanceDto dto) {
        AttendanceDto logged = attendanceService.logAttendance(dto);
        return new ResponseEntity<>(logged, HttpStatus.CREATED);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<AttendanceDto>> getEmployeeAttendance(
            @PathVariable Integer employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<AttendanceDto> history = attendanceService.getEmployeeAttendance(employeeId, page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<List<AttendanceDto>> getAttendanceInRange(
            @PathVariable Integer employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        List<AttendanceDto> history = attendanceService.getEmployeeAttendanceBetween(employeeId, start, end);
        return ResponseEntity.ok(history);
    }
}
