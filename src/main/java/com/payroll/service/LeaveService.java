package com.payroll.service;

import com.payroll.dto.LeaveDto;
import org.springframework.data.domain.Page;
import java.util.List;

public interface LeaveService {
    LeaveDto requestLeave(LeaveDto dto);
    void approveLeave(Integer leaveId, String status);
    Page<LeaveDto> getEmployeeLeaves(Integer employeeId, int page, int size);
    List<LeaveDto> getAllLeaves();
}
