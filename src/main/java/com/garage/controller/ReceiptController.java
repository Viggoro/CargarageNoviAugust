package com.garage.controller;

import com.garage.model.Employee;
import com.garage.model.Receipt;
import com.garage.service.PdfReceiptService;
import com.garage.service.ReceiptService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {
    
    private final ReceiptService receiptService;
    private final PdfReceiptService pdfReceiptService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<List<Receipt>> getAllReceipts() {
        return ResponseEntity.ok(receiptService.findAll());
    }
    
    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Page<Receipt>> getAllReceiptsPaged(Pageable pageable) {
        return ResponseEntity.ok(receiptService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Receipt> getReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.findById(id));
    }
    
    @PostMapping("/repair/{repairId}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Receipt> generateReceiptForRepair(
            @PathVariable Long repairId,
            @AuthenticationPrincipal Employee employee) {
        Receipt receipt = receiptService.generateReceiptForRepair(repairId, employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }
    
    @PostMapping("/inspection/{inspectionId}")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Receipt> generateReceiptForInspection(
            @PathVariable Long inspectionId,
            @Valid @RequestBody InspectionReceiptRequest request,
            @AuthenticationPrincipal Employee employee) {
        Receipt receipt = receiptService.generateReceiptForInspection(inspectionId, request.getInspectionFee(), employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }
    
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<Receipt> markAsPaid(
            @PathVariable Long id,
            @Valid @RequestBody MarkAsPaidRequest request,
            @AuthenticationPrincipal Employee employee) {
        Receipt receipt = receiptService.markAsPaid(id, request.getPaymentMethod(), employee);
        return ResponseEntity.ok(receipt);
    }
    
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('CASHIER', 'ADMIN', 'BACK_OFFICE')")
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable Long id) {
        Receipt receipt = receiptService.findById(id);
        byte[] pdfContent = pdfReceiptService.generateReceiptPdf(receipt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
    
    @Data
    public static class InspectionReceiptRequest {
        @NotNull(message = "Inspection fee is required")
        @Positive(message = "Inspection fee must be positive")
        private Double inspectionFee;
    }
    
    @Data
    public static class MarkAsPaidRequest {
        @NotBlank(message = "Payment method is required")
        private String paymentMethod;
    }
}