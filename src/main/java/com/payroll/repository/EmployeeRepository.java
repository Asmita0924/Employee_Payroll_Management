package com.payroll.repository;

import com.payroll.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmail(String email);

    Page<Employee> findByDepartmentId(Integer departmentId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:deptId IS NULL OR e.department.id = :deptId)")
    Page<Employee> searchEmployees(@Param("name") String name, 
                                   @Param("deptId") Integer deptId, 
                                   Pageable pageable);
}
