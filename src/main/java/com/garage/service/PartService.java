package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.Employee;
import com.garage.model.Part;
import com.garage.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartService {
    
    private final PartRepository partRepository;
    private final AuditLogService auditLogService;
    
    public Part findById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part", id));
    }
    
    public List<Part> findAll() {
        return partRepository.findAll();
    }
    
    public Page<Part> findAll(Pageable pageable) {
        return partRepository.findAll(pageable);
    }
    
    public List<Part> searchByName(String name) {
        return partRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Part> findLowStock(int threshold) {
        return partRepository.findByStockLessThan(threshold);
    }
    
    @Transactional
    public Part createPart(String name, Double price, Integer stock, Employee createdBy) {
        Part part = new Part();
        part.setName(name);
        part.setPrice(price);
        part.setStock(stock != null ? stock : 0);
        
        Part savedPart = partRepository.save(part);
        
        auditLogService.log("CREATE", createdBy, "Part", savedPart.getId(), 
                "Created part: " + name);
        
        return savedPart;
    }
    
    @Transactional
    public Part updatePart(Long id, String name, Double price, Integer stock, Employee updatedBy) {
        Part part = findById(id);
        
        if (name != null) part.setName(name);
        if (price != null) part.setPrice(price);
        if (stock != null) part.setStock(stock);
        
        Part savedPart = partRepository.save(part);
        
        auditLogService.log("UPDATE", updatedBy, "Part", id, 
                "Updated part: " + part.getName());
        
        return savedPart;
    }
    
    @Transactional
    public Part updatePrice(Long id, Double newPrice, Employee updatedBy) {
        Part part = findById(id);
        Double oldPrice = part.getPrice();
        part.setPrice(newPrice);
        
        Part savedPart = partRepository.save(part);
        
        auditLogService.log("PRICE_UPDATE", updatedBy, "Part", id, 
                "Updated price from " + oldPrice + " to " + newPrice);
        
        return savedPart;
    }
    
    @Transactional
    public Part updateStock(Long id, Integer newStock, Employee updatedBy) {
        Part part = findById(id);
        Integer oldStock = part.getStock();
        part.setStock(newStock);
        
        Part savedPart = partRepository.save(part);
        
        auditLogService.log("STOCK_UPDATE", updatedBy, "Part", id, 
                "Updated stock from " + oldStock + " to " + newStock);
        
        return savedPart;
    }
    
    @Transactional
    public Part decreaseStock(Long id, int quantity, Employee updatedBy) {
        Part part = findById(id);
        
        if (part.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for part: " + part.getName());
        }
        
        part.setStock(part.getStock() - quantity);
        
        Part savedPart = partRepository.save(part);
        
        auditLogService.log("STOCK_DECREASE", updatedBy, "Part", id, 
                "Decreased stock by " + quantity);
        
        return savedPart;
    }
    
    @Transactional
    public void deletePart(Long id, Employee deletedBy) {
        Part part = findById(id);
        partRepository.delete(part);
        
        auditLogService.log("DELETE", deletedBy, "Part", id, 
                "Deleted part: " + part.getName());
    }
}