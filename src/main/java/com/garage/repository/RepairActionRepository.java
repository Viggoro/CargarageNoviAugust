package com.garage.repository;

import com.garage.model.RepairAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairActionRepository extends JpaRepository<RepairAction, Long> {
    List<RepairAction> findByNameContainingIgnoreCase(String name);
    Page<RepairAction> findAll(Pageable pageable);
}