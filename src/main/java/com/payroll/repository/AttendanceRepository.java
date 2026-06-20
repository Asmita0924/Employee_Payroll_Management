package com.payroll.repository;

import com.payroll.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    Page<Attendance> findByEmployeeId(Integer employeeId, Pageable pageable);
    Optional<Attendance> findByEmployeeIdAndDate(Integer employeeId, LocalDate date);
    List<Attendance> findByEmployeeIdAndDateBetween(Integer employeeId, LocalDate startDate, LocalDate endDate);
}
