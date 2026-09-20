package com.garage.repository;

import com.garage.model.Repair;
import com.garage.model.enums.RepairStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {
    List<Repair> findByCarId(Long carId);
    List<Repair> findByPerformedById(Long mechanicId);
    List<Repair> findByStatus(RepairStatus status);
    Page<Repair> findByStatus(RepairStatus status, Pageable pageable);
    Page<Repair> findAll(Pageable pageable);
}