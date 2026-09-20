package com.garage.repository;

import com.garage.model.RepairPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairPartRepository extends JpaRepository<RepairPart, Long> {
    List<RepairPart> findByRepairId(Long repairId);
}