package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.*;
import com.garage.model.enums.Role;
import com.garage.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService implements UserDetailsService {
    
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return employeeRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
    
    public Employee findByUsername(String username) {
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + username));
    }
    
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }
    
    public List<Employee> findActiveEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }
    
    public List<Employee> findByRole(Role role) {
        return employeeRepository.findByRole(role);
    }
    
    @Transactional
    public Employee createEmployee(String username, String password, String email, String name, Role role, Employee createdBy) {
        if (employeeRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }
        if (employeeRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }
        
        Employee employee;
        switch (role) {
            case ADMIN -> employee = new AdminEmployee();
            case BACK_OFFICE -> employee = new BackOfficeEmployee();
            case CASHIER -> employee = new Cashier();
            case MECHANIC -> employee = new Mechanic();
            default -> throw new BadRequestException("Invalid role");
        }
        
        employee.setUsername(username);
        employee.setPasswordHash(passwordEncoder.encode(password));
        employee.setEmail(email);
        employee.setName(name);
        employee.setRole(role);
        employee.setIsActive(true);
        
        Employee savedEmployee = employeeRepository.save(employee);
        
        auditLogService.log("CREATE", createdBy, "Employee", savedEmployee.getId(), 
                "Created employee: " + username);
        
        return savedEmployee;
    }
    
    @Transactional
    public Employee updateEmployee(Long id, String email, String name, Boolean isActive, Employee updatedBy) {
        Employee employee = findById(id);
        
        if (email != null && !email.equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(email)) {
                throw new BadRequestException("Email already exists");
            }
            employee.setEmail(email);
        }
        
        if (name != null) {
            employee.setName(name);
        }
        
        if (isActive != null) {
            employee.setIsActive(isActive);
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        
        auditLogService.log("UPDATE", updatedBy, "Employee", savedEmployee.getId(), 
                "Updated employee: " + employee.getUsername());
        
        return savedEmployee;
    }
    
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword, Employee changedBy) {
        Employee employee = findById(id);
        
        if (!passwordEncoder.matches(oldPassword, employee.getPasswordHash())) {
            throw new BadRequestException("Invalid old password");
        }
        
        employee.setPasswordHash(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
        
        auditLogService.log("PASSWORD_CHANGE", changedBy, "Employee", id, 
                "Password changed for: " + employee.getUsername());
    }
    
    @Transactional
    public void deactivateEmployee(Long id, Employee deactivatedBy) {
        Employee employee = findById(id);
        employee.setIsActive(false);
        employeeRepository.save(employee);
        
        auditLogService.log("DEACTIVATE", deactivatedBy, "Employee", id, 
                "Deactivated employee: " + employee.getUsername());
    }
}