package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.Car;
import com.garage.model.Document;
import com.garage.model.Employee;
import com.garage.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final CarService carService;
    private final AuditLogService auditLogService;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";
    
    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }
    
    public List<Document> findByCarId(Long carId) {
        // Verify car exists
        carService.findById(carId);
        return documentRepository.findByCarId(carId);
    }
    
    @Transactional
    public Document uploadDocument(Long carId, MultipartFile file, Employee uploadedBy) {
        // Validate file
        validateFile(file);
        
        Car car = carService.findById(carId);
        
        try {
            Document document = new Document();
            document.setCar(car);
            document.setFilename(file.getOriginalFilename());
            document.setContentType(file.getContentType());
            document.setContent(file.getBytes());
            document.setEncrypted(false);
            
            Document savedDocument = documentRepository.save(document);
            
            auditLogService.log("UPLOAD", uploadedBy, "Document", savedDocument.getId(), 
                    "Uploaded document: " + file.getOriginalFilename() + " for car: " + car.getLicensePlate());
            
            return savedDocument;
        } catch (IOException e) {
            throw new BadRequestException("Failed to process file: " + e.getMessage());
        }
    }
    
    public byte[] downloadDocument(Long id) {
        Document document = findById(id);
        return document.getContent();
    }
    
    @Transactional
    public void deleteDocument(Long id, Employee deletedBy) {
        Document document = findById(id);
        String filename = document.getFilename();
        String licensePlate = document.getCar().getLicensePlate();
        
        documentRepository.delete(document);
        
        auditLogService.log("DELETE", deletedBy, "Document", id, 
                "Deleted document: " + filename + " for car: " + licensePlate);
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or null");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed size of 10MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(ALLOWED_CONTENT_TYPE)) {
            throw new BadRequestException("Only PDF files are allowed. Received content type: " + contentType);
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("File must have a .pdf extension");
        }
    }
}