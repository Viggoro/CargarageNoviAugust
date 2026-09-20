package com.garage.service;

import com.garage.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final EmployeeService employeeService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    
    public Map<String, Object> login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        
        Employee employee = employeeService.findByUsername(username);
        String token = jwtService.generateToken(employee);
        
        auditLogService.log("LOGIN", employee, "Employee", employee.getId(), 
                "User logged in: " + username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", employee.getId());
        response.put("username", employee.getUsername());
        response.put("name", employee.getName());
        response.put("email", employee.getEmail());
        response.put("role", employee.getRole().name());
        
        return response;
    }
}