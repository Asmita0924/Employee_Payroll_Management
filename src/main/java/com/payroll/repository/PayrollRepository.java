package com.payroll.repository;

import com.payroll.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Integer> {
    Page<Payroll> findByEmployeeId(Integer employeeId, Pageable pageable);
    Optional<Payroll> findByEmployeeIdAndSalaryMonth(Integer employeeId, String salaryMonth);
    Page<Payroll> findBySalaryMonth(String salaryMonth, Pageable pageable);
}
