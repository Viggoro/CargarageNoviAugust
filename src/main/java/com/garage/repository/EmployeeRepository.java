package com.garage.repository;

import com.garage.model.Employee;
import com.garage.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByUsernameAndIsActiveTrue(String username);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByRole(Role role);
    List<Employee> findByIsActiveTrue();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}