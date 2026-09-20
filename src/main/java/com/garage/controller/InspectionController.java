package com.garage.controller;

import com.garage.model.Employee;
import com.garage.model.Inspection;
import com.garage.model.enums.RepairStatus;
import com.garage.service.InspectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {
    
    private final InspectionService inspectionService;
    
    @GetMapping
    public ResponseEntity<List<Inspection>> getAllInspections() {
        return ResponseEntity.ok(inspectionService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Inspection> getInspectionById(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.findById(id));
    }
    
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<Inspection>> getInspectionsByCarId(@PathVariable Long carId) {
        return ResponseEntity.ok(inspectionService.findByCarId(carId));
    }
    
    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<List<Inspection>> getInspectionsByMechanic(@PathVariable Long mechanicId) {
        return ResponseEntity.ok(inspectionService.findByMechanicId(mechanicId));
    }
    
    @GetMapping("/scheduled")
    public ResponseEntity<List<Inspection>> getScheduledInspections() {
        return ResponseEntity.ok(inspectionService.findScheduledInspections());
    }
    
    @GetMapping("/mechanic/{mechanicId}/scheduled")
    public ResponseEntity<List<Inspection>> getScheduledInspectionsForMechanic(@PathVariable Long mechanicId) {
        return ResponseEntity.ok(inspectionService.findScheduledInspectionsForMechanic(mechanicId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Inspection>> getInspectionsByStatus(
            @PathVariable RepairStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(inspectionService.findByStatus(status, pageable));
    }
    
    @PostMapping
    public ResponseEntity<Inspection> scheduleInspection(
            @Valid @RequestBody ScheduleInspectionRequest request,
            @AuthenticationPrincipal Employee employee) {
        Inspection inspection = inspectionService.scheduleInspection(
                request.getCarId(),
                request.getMechanicId(),
                request.getScheduledAt(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(inspection);
    }
    
    @PostMapping("/{id}/defect")
    public ResponseEntity<Inspection> addDefect(
            @PathVariable Long id,
            @Valid @RequestBody AddDefectRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(inspectionService.addDefect(id, request.getDefect(), employee));
    }
    
    @PutMapping("/{id}/findings")
    public ResponseEntity<Inspection> setFindings(
            @PathVariable Long id,
            @Valid @RequestBody SetFindingsRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(inspectionService.setFindings(id, request.getFindings(), employee));
    }
    
    @PutMapping("/{id}/result")
    public ResponseEntity<Inspection> setInspectionResult(
            @PathVariable Long id,
            @Valid @RequestBody SetResultRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(inspectionService.setInspectionResult(id, request.getResult(), employee));
    }
    
    @PutMapping("/{id}/approval")
    public ResponseEntity<Inspection> setApprovalStatus(
            @PathVariable Long id,
            @Valid @RequestBody SetApprovalRequest request,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(inspectionService.setApprovalStatus(id, request.getApproved(), employee));
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<Inspection> startInspection(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(inspectionService.startInspection(id, employee));
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<Inspection> completeInspection(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(inspectionService.completeInspection(id, employee));
    }
    
    @Data
    public static class ScheduleInspectionRequest {
        @NotNull(message = "Car ID is required")
        private Long carId;
        
        @NotNull(message = "Mechanic ID is required")
        private Long mechanicId;
        
        @NotNull(message = "Scheduled date/time is required")
        private LocalDateTime scheduledAt;
    }
    
    @Data
    public static class AddDefectRequest {
        @NotBlank(message = "Defect description is required")
        private String defect;
    }
    
    @Data
    public static class SetFindingsRequest {
        @NotBlank(message = "Findings are required")
        private String findings;
    }
    
    @Data
    public static class SetResultRequest {
        @NotBlank(message = "Result is required")
        private String result;
    }
    
    @Data
    public static class SetApprovalRequest {
        @NotNull(message = "Approval status is required")
        private Boolean approved;
    }
}