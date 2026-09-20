package com.garage.controller;

import com.garage.model.Employee;
import com.garage.model.RepairAction;
import com.garage.service.RepairActionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repair-actions")
@RequiredArgsConstructor
public class RepairActionController {
    
    private final RepairActionService repairActionService;
    
    @GetMapping
    public ResponseEntity<List<RepairAction>> getAllRepairActions() {
        return ResponseEntity.ok(repairActionService.findAll());
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<RepairAction>> getAllRepairActionsPaged(Pageable pageable) {
        return ResponseEntity.ok(repairActionService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RepairAction> getRepairActionById(@PathVariable Long id) {
        return ResponseEntity.ok(repairActionService.findById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<RepairAction>> searchRepairActions(@RequestParam String name) {
        return ResponseEntity.ok(repairActionService.searchByName(name));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<RepairAction> createRepairAction(
            @Valid @RequestBody CreateRepairActionRequest request,
            @AuthenticationPrincipal Employee employee) {
        RepairAction action = repairActionService.createRepairAction(
                request.getName(),
                request.getPrice(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(action);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<RepairAction> updateRepairAction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRepairActionRequest request,
            @AuthenticationPrincipal Employee employee) {
        RepairAction action = repairActionService.updateRepairAction(
                id,
                request.getName(),
                request.getPrice(),
                employee
        );
        return ResponseEntity.ok(action);
    }
    
    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<RepairAction> updateRepairActionPrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriceRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(repairActionService.updatePrice(id, request.getPrice(), employee));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Void> deleteRepairAction(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        repairActionService.deleteRepairAction(id, employee);
        return ResponseEntity.noContent().build();
    }
    
    @Data
    public static class CreateRepairActionRequest {
        @NotBlank(message = "Name is required")
        private String name;
        
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        private Double price;
    }
    
    @Data
    public static class UpdateRepairActionRequest {
        private String name;
        
        @PositiveOrZero(message = "Price must be zero or positive")
        private Double price;
    }
    
    @Data
    public static class UpdatePriceRequest {
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        private Double price;
    }
}