package com.garage.repository;

import com.garage.model.CustomRepairAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomRepairActionRepository extends JpaRepository<CustomRepairAction, Long> {
    List<CustomRepairAction> findByRepairId(Long repairId);
}