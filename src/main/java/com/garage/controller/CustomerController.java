package com.garage.controller;

import com.garage.model.Customer;
import com.garage.model.Employee;
import com.garage.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<Customer>> getAllCustomersPaged(Pageable pageable) {
        return ResponseEntity.ok(customerService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Customer>> searchCustomers(
            @RequestParam String lastName, 
            Pageable pageable) {
        return ResponseEntity.ok(customerService.searchByLastName(lastName, pageable));
    }
    
    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            @AuthenticationPrincipal Employee employee) {
        Customer customer = customerService.createCustomer(
                request.getFirstName(),
                request.getLastName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getEmail(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request,
            @AuthenticationPrincipal Employee employee) {
        Customer customer = customerService.updateCustomer(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getEmail(),
                employee
        );
        return ResponseEntity.ok(customer);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        customerService.deleteCustomer(id, employee);
        return ResponseEntity.noContent().build();
    }
    
    @Data
    public static class CreateCustomerRequest {
        @NotBlank(message = "First name is required")
        private String firstName;
        
        @NotBlank(message = "Last name is required")
        private String lastName;
        
        private String address;
        private String phoneNumber;
        
        @Email(message = "Invalid email format")
        private String email;
    }
    
    @Data
    public static class UpdateCustomerRequest {
        private String firstName;
        private String lastName;
        private String address;
        private String phoneNumber;
        
        @Email(message = "Invalid email format")
        private String email;
    }
}