package com.garage.repository;

import com.garage.model.Inspection;
import com.garage.model.enums.RepairStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByCarId(Long carId);
    List<Inspection> findByPerformedById(Long mechanicId);
    List<Inspection> findByStatus(RepairStatus status);
    Page<Inspection> findByStatus(RepairStatus status, Pageable pageable);
    List<Inspection> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
    List<Inspection> findByPerformedByIdAndStatus(Long mechanicId, RepairStatus status);
}