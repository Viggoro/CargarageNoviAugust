package com.garage.repository;

import com.garage.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Page<Customer> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    boolean existsByEmail(String email);
}