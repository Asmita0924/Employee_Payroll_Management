package com.payroll.repository;

import com.payroll.entity.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Integer> {
    Page<Leave> findByEmployeeId(Integer employeeId, Pageable pageable);
    List<Leave> findByEmployeeIdAndStatus(Integer employeeId, String status);
}
