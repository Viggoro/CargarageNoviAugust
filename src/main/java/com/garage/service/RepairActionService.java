package com.garage.service;

import com.garage.exception.ResourceNotFoundException;
import com.garage.model.Employee;
import com.garage.model.RepairAction;
import com.garage.repository.RepairActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairActionService {
    
    private final RepairActionRepository repairActionRepository;
    private final AuditLogService auditLogService;
    
    public RepairAction findById(Long id) {
        return repairActionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RepairAction", id));
    }
    
    public List<RepairAction> findAll() {
        return repairActionRepository.findAll();
    }
    
    public Page<RepairAction> findAll(Pageable pageable) {
        return repairActionRepository.findAll(pageable);
    }
    
    public List<RepairAction> searchByName(String name) {
        return repairActionRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Transactional
    public RepairAction createRepairAction(String name, Double price, Employee createdBy) {
        RepairAction action = new RepairAction();
        action.setName(name);
        action.setPrice(price);
        
        RepairAction savedAction = repairActionRepository.save(action);
        
        auditLogService.log("CREATE", createdBy, "RepairAction", savedAction.getId(), 
                "Created repair action: " + name);
        
        return savedAction;
    }
    
    @Transactional
    public RepairAction updateRepairAction(Long id, String name, Double price, Employee updatedBy) {
        RepairAction action = findById(id);
        
        if (name != null) action.setName(name);
        if (price != null) action.setPrice(price);
        
        RepairAction savedAction = repairActionRepository.save(action);
        
        auditLogService.log("UPDATE", updatedBy, "RepairAction", id, 
                "Updated repair action: " + action.getName());
        
        return savedAction;
    }
    
    @Transactional
    public RepairAction updatePrice(Long id, Double newPrice, Employee updatedBy) {
        RepairAction action = findById(id);
        Double oldPrice = action.getPrice();
        action.setPrice(newPrice);
        
        RepairAction savedAction = repairActionRepository.save(action);
        
        auditLogService.log("PRICE_UPDATE", updatedBy, "RepairAction", id, 
                "Updated price from " + oldPrice + " to " + newPrice);
        
        return savedAction;
    }
    
    @Transactional
    public void deleteRepairAction(Long id, Employee deletedBy) {
        RepairAction action = findById(id);
        repairActionRepository.delete(action);
        
        auditLogService.log("DELETE", deletedBy, "RepairAction", id, 
                "Deleted repair action: " + action.getName());
    }
}