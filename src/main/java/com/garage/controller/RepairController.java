package com.garage.controller;

import com.garage.model.Employee;
import com.garage.model.Repair;
import com.garage.model.enums.RepairStatus;
import com.garage.service.RepairService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
public class RepairController {
    
    private final RepairService repairService;
    
    @GetMapping
    public ResponseEntity<List<Repair>> getAllRepairs() {
        return ResponseEntity.ok(repairService.findAll());
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<Repair>> getAllRepairsPaged(Pageable pageable) {
        return ResponseEntity.ok(repairService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Repair> getRepairById(@PathVariable Long id) {
        return ResponseEntity.ok(repairService.findById(id));
    }
    
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<Repair>> getRepairsByCarId(@PathVariable Long carId) {
        return ResponseEntity.ok(repairService.findByCarId(carId));
    }
    
    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<List<Repair>> getRepairsByMechanic(@PathVariable Long mechanicId) {
        return ResponseEntity.ok(repairService.findByMechanicId(mechanicId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Repair>> getRepairsByStatus(
            @PathVariable RepairStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(repairService.findByStatus(status, pageable));
    }
    
    @PostMapping
    public ResponseEntity<Repair> createRepair(
            @Valid @RequestBody CreateRepairRequest request,
            @AuthenticationPrincipal Employee employee) {
        Repair repair = repairService.createRepair(
                request.getCarId(),
                request.getMechanicId(),
                request.getScheduledAt(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(repair);
    }
    
    @PostMapping("/{id}/parts")
    public ResponseEntity<Repair> addPartToRepair(
            @PathVariable Long id,
            @Valid @RequestBody AddPartRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.addPartToRepair(id, request.getPartId(), request.getQuantity(), employee));
    }
    
    @PostMapping("/{id}/actions")
    public ResponseEntity<Repair> addActionToRepair(
            @PathVariable Long id,
            @Valid @RequestBody AddActionRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.addActionToRepair(id, request.getActionId(), employee));
    }
    
    @PostMapping("/{id}/custom-actions")
    public ResponseEntity<Repair> addCustomActionToRepair(
            @PathVariable Long id,
            @Valid @RequestBody AddCustomActionRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.addCustomActionToRepair(id, request.getDescription(), request.getPrice(), employee));
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<Repair> startRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.startRepair(id, employee));
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<Repair> completeRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.completeRepair(id, employee));
    }
    
    @PutMapping("/{id}/not-performed")
    public ResponseEntity<Repair> markAsNotPerformed(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.markAsNotPerformed(id, employee));
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Repair> cancelRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairService.cancelRepair(id, employee));
    }
    
    @Data
    public static class CreateRepairRequest {
        @NotNull(message = "Car ID is required")
        private Long carId;
        
        @NotNull(message = "Mechanic ID is required")
        private Long mechanicId;
        
        @NotNull(message = "Scheduled date/time is required")
        private LocalDateTime scheduledAt;
    }
    
    @Data
    public static class AddPartRequest {
        @NotNull(message = "Part ID is required")
        private Long partId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }
    
    @Data
    public static class AddActionRequest {
        @NotNull(message = "Action ID is required")
        private Long actionId;
    }
    
    @Data
    public static class AddCustomActionRequest {
        @NotBlank(message = "Description is required")
        private String description;
        
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private Double price;
    }
}