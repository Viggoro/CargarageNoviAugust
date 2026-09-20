package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.*;
import com.garage.model.enums.PaymentStatus;
import com.garage.model.enums.RepairStatus;
import com.garage.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    
    private static final double VAT_RATE = 0.21; // 21% VAT
    
    private final ReceiptRepository receiptRepository;
    private final RepairService repairService;
    private final InspectionService inspectionService;
    private final AuditLogService auditLogService;
    
    public Receipt findById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", id));
    }
    
    public List<Receipt> findAll() {
        return receiptRepository.findAll();
    }
    
    public Page<Receipt> findAll(Pageable pageable) {
        return receiptRepository.findAll(pageable);
    }
    
    @Transactional
    public Receipt generateReceiptForRepair(Long repairId, Employee createdBy) {
        Repair repair = repairService.findById(repairId);
        
        if (repair.getStatus() != RepairStatus.COMPLETED) {
            throw new BadRequestException("Repair is not completed");
        }
        
        if (receiptRepository.findByRepairId(repairId).isPresent()) {
            throw new BadRequestException("Receipt already exists for this repair");
        }
        
        repair.calculateTotalCost();
        double total = repair.getTotalCost();
        double vat = total * VAT_RATE;
        
        Payment payment = new Payment();
        payment.setAmount(total + vat);
        payment.setVatAmount(vat);
        payment.setStatus(PaymentStatus.PENDING);
        
        Receipt receipt = new Receipt();
        receipt.setRepair(repair);
        receipt.setTotal(total);
        receipt.setVat(vat);
        receipt.setPayment(payment);
        payment.setReceipt(receipt);
        
        Receipt savedReceipt = receiptRepository.save(receipt);
        
        auditLogService.log("CREATE", createdBy, "Receipt", savedReceipt.getId(), 
                "Generated receipt for repair: " + repairId);
        
        return savedReceipt;
    }
    
    @Transactional
    public Receipt generateReceiptForInspection(Long inspectionId, Double inspectionFee, Employee createdBy) {
        Inspection inspection = inspectionService.findById(inspectionId);
        
        if (inspection.getStatus() != RepairStatus.COMPLETED && 
            inspection.getStatus() != RepairStatus.NOT_PERFORMED) {
            throw new BadRequestException("Inspection is not completed");
        }
        
        if (receiptRepository.findByInspectionId(inspectionId).isPresent()) {
            throw new BadRequestException("Receipt already exists for this inspection");
        }
        
        double total = inspectionFee;
        double vat = total * VAT_RATE;
        
        Payment payment = new Payment();
        payment.setAmount(total + vat);
        payment.setVatAmount(vat);
        payment.setStatus(PaymentStatus.PENDING);
        
        Receipt receipt = new Receipt();
        receipt.setInspection(inspection);
        receipt.setTotal(total);
        receipt.setVat(vat);
        receipt.setPayment(payment);
        payment.setReceipt(receipt);
        
        Receipt savedReceipt = receiptRepository.save(receipt);
        
        auditLogService.log("CREATE", createdBy, "Receipt", savedReceipt.getId(), 
                "Generated receipt for inspection: " + inspectionId);
        
        return savedReceipt;
    }
    
    @Transactional
    public Receipt markAsPaid(Long receiptId, String paymentMethod, Employee updatedBy) {
        Receipt receipt = findById(receiptId);
        Payment payment = receipt.getPayment();
        
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Receipt is already paid");
        }
        
        payment.setMethod(paymentMethod);
        payment.markAsPaid();
        
        Receipt savedReceipt = receiptRepository.save(receipt);
        
        auditLogService.log("PAYMENT", updatedBy, "Receipt", receiptId, 
                "Marked receipt as paid with method: " + paymentMethod);
        
        return savedReceipt;
    }
}