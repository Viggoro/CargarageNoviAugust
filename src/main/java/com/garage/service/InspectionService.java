package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.*;
import com.garage.model.enums.RepairStatus;
import com.garage.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService {
    
    private final InspectionRepository inspectionRepository;
    private final CarService carService;
    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;
    
    public Inspection findById(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", id));
    }
    
    public List<Inspection> findAll() {
        return inspectionRepository.findAll();
    }
    
    public List<Inspection> findByCarId(Long carId) {
        return inspectionRepository.findByCarId(carId);
    }
    
    public List<Inspection> findByMechanicId(Long mechanicId) {
        return inspectionRepository.findByPerformedById(mechanicId);
    }
    
    public List<Inspection> findScheduledInspections() {
        return inspectionRepository.findByStatus(RepairStatus.SCHEDULED);
    }
    
    public List<Inspection> findScheduledInspectionsForMechanic(Long mechanicId) {
        return inspectionRepository.findByPerformedByIdAndStatus(mechanicId, RepairStatus.SCHEDULED);
    }
    
    public Page<Inspection> findByStatus(RepairStatus status, Pageable pageable) {
        return inspectionRepository.findByStatus(status, pageable);
    }
    
    @Transactional
    public Inspection scheduleInspection(Long carId, Long mechanicId, LocalDateTime scheduledAt, Employee createdBy) {
        Car car = carService.findById(carId);
        Employee mechanic = employeeService.findById(mechanicId);
        
        if (!(mechanic instanceof Mechanic)) {
            throw new BadRequestException("Employee is not a mechanic");
        }
        
        Inspection inspection = new Inspection();
        inspection.setCar(car);
        inspection.setPerformedBy((Mechanic) mechanic);
        inspection.setScheduledAt(scheduledAt);
        inspection.setStatus(RepairStatus.SCHEDULED);
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("CREATE", createdBy, "Inspection", savedInspection.getId(), 
                "Scheduled inspection for car: " + car.getLicensePlate());
        
        return savedInspection;
    }
    
    @Transactional
    public Inspection addDefect(Long inspectionId, String defect, Employee updatedBy) {
        Inspection inspection = findById(inspectionId);
        inspection.addDefect(defect);
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("UPDATE", updatedBy, "Inspection", inspectionId, 
                "Added defect: " + defect);
        
        return savedInspection;
    }
    
    @Transactional
    public Inspection setFindings(Long inspectionId, String findings, Employee updatedBy) {
        Inspection inspection = findById(inspectionId);
        inspection.setFindings(findings);
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("UPDATE", updatedBy, "Inspection", inspectionId, 
                "Set inspection findings");
        
        return savedInspection;
    }
    
    @Transactional
    public Inspection setInspectionResult(Long inspectionId, String result, Employee updatedBy) {
        Inspection inspection = findById(inspectionId);
        inspection.setInspectionResult(result);
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("UPDATE", updatedBy, "Inspection", inspectionId, 
                "Set inspection result: " + result);
        
        return savedInspection;
    }
    
    @Transactional
    public Inspection setApprovalStatus(Long inspectionId, Boolean approved, Employee updatedBy) {
        Inspection inspection = findById(inspectionId);
        inspection.setApproved(approved);
        
        if (approved) {
            inspection.setStatus(RepairStatus.COMPLETED);
            inspection.setCompletedAt(LocalDateTime.now());
        }
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("UPDATE", updatedBy, "Inspection", inspectionId, 
                "Set approval status: " + (approved ? "Approved" : "Not Approved"));
        
        return savedInspection;
    }
    
    @Transactional
    public Inspection startInspection(Long inspectionId, Employee updatedBy) {
        Inspection inspection = findById(inspectionId);
        inspection.setStatus(RepairStatus.IN_PROGRESS);
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("UPDATE", updatedBy, "Inspection", inspectionId, 
                "Started inspection");
        
        return savedInspection;
    }
    
    @Transactional
    public Inspection completeInspection(Long inspectionId, Employee updatedBy) {
        Inspection inspection = findById(inspectionId);
        inspection.setStatus(RepairStatus.COMPLETED);
        inspection.setCompletedAt(LocalDateTime.now());
        
        Inspection savedInspection = inspectionRepository.save(inspection);
        
        auditLogService.log("UPDATE", updatedBy, "Inspection", inspectionId, 
                "Completed inspection");
        
        return savedInspection;
    }
}