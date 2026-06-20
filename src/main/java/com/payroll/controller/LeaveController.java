package com.payroll.controller;

import com.payroll.dto.LeaveDto;
import com.payroll.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveDto> requestLeave(@Valid @RequestBody LeaveDto dto) {
        LeaveDto created = leaveService.requestLeave(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Map<String, String>> approveLeave(@PathVariable Integer id, @RequestParam String status) {
        leaveService.approveLeave(id, status);
        return ResponseEntity.ok(Map.of("message", "Leave request has been successfully updated via stored procedure logic. Status: " + status));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<LeaveDto>> getEmployeeLeaves(
            @PathVariable Integer employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<LeaveDto> history = leaveService.getEmployeeLeaves(employeeId, page, size);
        return ResponseEntity.ok(history);
    }

    @GetMapping
    public ResponseEntity<List<LeaveDto>> getAllLeaves() {
        List<LeaveDto> list = leaveService.getAllLeaves();
        return ResponseEntity.ok(list);
    }
}
