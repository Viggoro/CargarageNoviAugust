package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.Customer;
import com.garage.model.Employee;
import com.garage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;
    
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
    
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
    
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
    
    public Page<Customer> searchByLastName(String lastName, Pageable pageable) {
        return customerRepository.findByLastNameContainingIgnoreCase(lastName, pageable);
    }
    
    @Transactional
    public Customer createCustomer(String firstName, String lastName, String address, 
                                   String phoneNumber, String email, Employee createdBy) {
        if (email != null && customerRepository.existsByEmail(email)) {
            throw new BadRequestException("Customer with this email already exists");
        }
        
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAddress(address);
        customer.setPhoneNumber(phoneNumber);
        customer.setEmail(email);
        
        Customer savedCustomer = customerRepository.save(customer);
        
        auditLogService.log("CREATE", createdBy, "Customer", savedCustomer.getId(), 
                "Created customer: " + firstName + " " + lastName);
        
        return savedCustomer;
    }
    
    @Transactional
    public Customer updateCustomer(Long id, String firstName, String lastName, String address, 
                                   String phoneNumber, String email, Employee updatedBy) {
        Customer customer = findById(id);
        
        if (email != null && !email.equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(email)) {
                throw new BadRequestException("Customer with this email already exists");
            }
            customer.setEmail(email);
        }
        
        if (firstName != null) customer.setFirstName(firstName);
        if (lastName != null) customer.setLastName(lastName);
        if (address != null) customer.setAddress(address);
        if (phoneNumber != null) customer.setPhoneNumber(phoneNumber);
        
        Customer savedCustomer = customerRepository.save(customer);
        
        auditLogService.log("UPDATE", updatedBy, "Customer", savedCustomer.getId(), 
                "Updated customer: " + customer.getFirstName() + " " + customer.getLastName());
        
        return savedCustomer;
    }
    
    @Transactional
    public void deleteCustomer(Long id, Employee deletedBy) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
        
        auditLogService.log("DELETE", deletedBy, "Customer", id, 
                "Deleted customer: " + customer.getFirstName() + " " + customer.getLastName());
    }
}