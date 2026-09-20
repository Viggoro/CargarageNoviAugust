package com.garage.controller;

import com.garage.model.Employee;
import com.garage.model.Part;
import com.garage.service.PartService;
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
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {
    
    private final PartService partService;
    
    @GetMapping
    public ResponseEntity<List<Part>> getAllParts() {
        return ResponseEntity.ok(partService.findAll());
    }
    
    @GetMapping("/paged")
    public ResponseEntity<Page<Part>> getAllPartsPaged(Pageable pageable) {
        return ResponseEntity.ok(partService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Part> getPartById(@PathVariable Long id) {
        return ResponseEntity.ok(partService.findById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Part>> searchParts(@RequestParam String name) {
        return ResponseEntity.ok(partService.searchByName(name));
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<Part>> getLowStockParts(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(partService.findLowStock(threshold));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Part> createPart(
            @Valid @RequestBody CreatePartRequest request,
            @AuthenticationPrincipal Employee employee) {
        Part part = partService.createPart(
                request.getName(),
                request.getPrice(),
                request.getStock(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(part);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Part> updatePart(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartRequest request,
            @AuthenticationPrincipal Employee employee) {
        Part part = partService.updatePart(
                id,
                request.getName(),
                request.getPrice(),
                request.getStock(),
                employee
        );
        return ResponseEntity.ok(part);
    }
    
    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Part> updatePartPrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriceRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(partService.updatePrice(id, request.getPrice(), employee));
    }
    
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Part> updatePartStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(partService.updateStock(id, request.getStock(), employee));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Void> deletePart(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        partService.deletePart(id, employee);
        return ResponseEntity.noContent().build();
    }
    
    @Data
    public static class CreatePartRequest {
        @NotBlank(message = "Name is required")
        private String name;
        
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        private Double price;
        
        @PositiveOrZero(message = "Stock must be zero or positive")
        private Integer stock;
    }
    
    @Data
    public static class UpdatePartRequest {
        private String name;
        
        @PositiveOrZero(message = "Price must be zero or positive")
        private Double price;
        
        @PositiveOrZero(message = "Stock must be zero or positive")
        private Integer stock;
    }
    
    @Data
    public static class UpdatePriceRequest {
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be zero or positive")
        private Double price;
    }
    
    @Data
    public static class UpdateStockRequest {
        @NotNull(message = "Stock is required")
        @PositiveOrZero(message = "Stock must be zero or positive")
        private Integer stock;
    }
}