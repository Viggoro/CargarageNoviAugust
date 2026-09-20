package com.garage.controller;

import com.garage.model.Employee;
import com.garage.model.enums.Role;
import com.garage.service.AuditLogService;
import com.garage.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;
    
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Employee>> getActiveEmployees() {
        return ResponseEntity.ok(employeeService.findActiveEmployees());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Employee>> getEmployeesByRole(@PathVariable Role role) {
        return ResponseEntity.ok(employeeService.findByRole(role));
    }
    
    @PostMapping
    public ResponseEntity<Employee> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal Employee employee) {
        Employee newEmployee = employeeService.createEmployee(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getName(),
                request.getRole(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @AuthenticationPrincipal Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(
                id,
                request.getEmail(),
                request.getName(),
                request.getIsActive(),
                employee
        );
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Employee employee) {
        employeeService.changePassword(id, request.getOldPassword(), request.getNewPassword(), employee);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        employeeService.deactivateEmployee(id, employee);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<com.garage.model.AuditLog>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.findAll(pageable));
    }
    
    @Data
    public static class CreateEmployeeRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Password is required")
        private String password;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        
        @NotBlank(message = "Name is required")
        private String name;
        
        @NotNull(message = "Role is required")
        private Role role;
    }
    
    @Data
    public static class UpdateEmployeeRequest {
        @Email(message = "Invalid email format")
        private String email;
        
        private String name;
        private Boolean isActive;
    }
    
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "Old password is required")
        private String oldPassword;
        
        @NotBlank(message = "New password is required")
        private String newPassword;
    }
}