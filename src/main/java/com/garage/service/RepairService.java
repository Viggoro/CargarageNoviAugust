package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.*;
import com.garage.model.enums.RepairStatus;
import com.garage.repository.RepairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairService {
    
    private final RepairRepository repairRepository;
    private final CarService carService;
    private final EmployeeService employeeService;
    private final PartService partService;
    private final RepairActionService repairActionService;
    private final AuditLogService auditLogService;
    
    public Repair findById(Long id) {
        return repairRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repair", id));
    }
    
    public List<Repair> findAll() {
        return repairRepository.findAll();
    }
    
    public Page<Repair> findAll(Pageable pageable) {
        return repairRepository.findAll(pageable);
    }
    
    public List<Repair> findByCarId(Long carId) {
        return repairRepository.findByCarId(carId);
    }
    
    public List<Repair> findByMechanicId(Long mechanicId) {
        return repairRepository.findByPerformedById(mechanicId);
    }
    
    public List<Repair> findByStatus(RepairStatus status) {
        return repairRepository.findByStatus(status);
    }
    
    public Page<Repair> findByStatus(RepairStatus status, Pageable pageable) {
        return repairRepository.findByStatus(status, pageable);
    }
    
    @Transactional
    public Repair createRepair(Long carId, Long mechanicId, LocalDateTime scheduledAt, Employee createdBy) {
        Car car = carService.findById(carId);
        Employee mechanic = employeeService.findById(mechanicId);
        
        if (!(mechanic instanceof Mechanic)) {
            throw new BadRequestException("Employee is not a mechanic");
        }
        
        Repair repair = new Repair();
        repair.setCar(car);
        repair.setPerformedBy((Mechanic) mechanic);
        repair.setScheduledAt(scheduledAt);
        repair.setStatus(RepairStatus.SCHEDULED);
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("CREATE", createdBy, "Repair", savedRepair.getId(), 
                "Created repair for car: " + car.getLicensePlate());
        
        return savedRepair;
    }
    
    @Transactional
    public Repair addPartToRepair(Long repairId, Long partId, int quantity, Employee updatedBy) {
        Repair repair = findById(repairId);
        Part part = partService.findById(partId);
        
        if (part.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for part: " + part.getName());
        }
        
        repair.addPart(part, quantity);
        partService.decreaseStock(partId, quantity, updatedBy);
        repair.calculateTotalCost();
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Added part: " + part.getName() + " x" + quantity);
        
        return savedRepair;
    }
    
    @Transactional
    public Repair addActionToRepair(Long repairId, Long actionId, Employee updatedBy) {
        Repair repair = findById(repairId);
        RepairAction action = repairActionService.findById(actionId);
        
        repair.addAction(action);
        repair.calculateTotalCost();
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Added action: " + action.getName());
        
        return savedRepair;
    }
    
    @Transactional
    public Repair addCustomActionToRepair(Long repairId, String description, Double price, Employee updatedBy) {
        Repair repair = findById(repairId);
        
        CustomRepairAction customAction = new CustomRepairAction();
        customAction.setDescription(description);
        customAction.setPrice(price);
        customAction.setRepair(repair);
        
        repair.addCustomAction(customAction);
        repair.calculateTotalCost();
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Added custom action: " + description);
        
        return savedRepair;
    }
    
    @Transactional
    public Repair startRepair(Long repairId, Employee updatedBy) {
        Repair repair = findById(repairId);
        repair.setStatus(RepairStatus.IN_PROGRESS);
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Started repair");
        
        return savedRepair;
    }
    
    @Transactional
    public Repair completeRepair(Long repairId, Employee updatedBy) {
        Repair repair = findById(repairId);
        repair.setStatus(RepairStatus.COMPLETED);
        repair.setCompletedAt(LocalDateTime.now());
        repair.calculateTotalCost();
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Completed repair");
        
        return savedRepair;
    }
    
    @Transactional
    public Repair markAsNotPerformed(Long repairId, Employee updatedBy) {
        Repair repair = findById(repairId);
        repair.setStatus(RepairStatus.NOT_PERFORMED);
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Marked repair as not performed");
        
        return savedRepair;
    }
    
    @Transactional
    public Repair cancelRepair(Long repairId, Employee updatedBy) {
        Repair repair = findById(repairId);
        repair.setStatus(RepairStatus.CANCELLED);
        
        Repair savedRepair = repairRepository.save(repair);
        
        auditLogService.log("UPDATE", updatedBy, "Repair", repairId, 
                "Cancelled repair");
        
        return savedRepair;
    }
}