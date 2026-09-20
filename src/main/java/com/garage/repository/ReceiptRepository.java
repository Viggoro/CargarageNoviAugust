package com.garage.repository;

import com.garage.model.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByRepairId(Long repairId);
    Optional<Receipt> findByInspectionId(Long inspectionId);
    Page<Receipt> findAll(Pageable pageable);
}