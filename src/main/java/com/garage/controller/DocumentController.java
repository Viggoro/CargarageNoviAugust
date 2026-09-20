package com.garage.controller;

import com.garage.model.Document;
import com.garage.model.Employee;
import com.garage.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Vehicle document management API - administrative employees can upload PDF documents")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @GetMapping("/car/{carId}")
    @Operation(summary = "Get all documents for a specific car", description = "Retrieves a list of all documents associated with a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved documents"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<List<DocumentDto>> getDocumentsByCarId(
            @Parameter(description = "ID of the car") @PathVariable Long carId) {
        List<DocumentDto> documents = documentService.findByCarId(carId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get document metadata by ID", description = "Retrieves document information without the file content")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved document"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<DocumentDto> getDocumentById(
            @Parameter(description = "ID of the document") @PathVariable Long id) {
        Document document = documentService.findById(id);
        return ResponseEntity.ok(convertToDto(document));
    }
    
    @GetMapping("/{id}/download")
    @Operation(summary = "Download document file", description = "Downloads the actual PDF file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully downloaded document", 
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<byte[]> downloadDocument(
            @Parameter(description = "ID of the document") @PathVariable Long id) {
        Document document = documentService.findById(id);
        byte[] content = documentService.downloadDocument(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFilename() + "\"")
                .body(content);
    }
    
    @PostMapping("/car/{carId}/upload")
    @Operation(summary = "Upload a PDF document for a vehicle", 
            description = "Administrative employees can upload PDF documents to a vehicle's record. Only PDF files are accepted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or file is not a PDF"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires BACK_OFFICE role"),
            @ApiResponse(responseCode = "404", description = "Car not found")
    })
    public ResponseEntity<DocumentDto> uploadDocument(
            @Parameter(description = "ID of the car") @PathVariable Long carId,
            @Parameter(description = "PDF file to upload", required = true,
                    content = @Content(mediaType = "multipart/form-data")) 
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal Employee employee) {
        Document document = documentService.uploadDocument(carId, file, employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(document));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document", description = "Administrative employees can delete documents from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires BACK_OFFICE role"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID of the document") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Employee employee) {
        documentService.deleteDocument(id, employee);
        return ResponseEntity.noContent().build();
    }
    
    private DocumentDto convertToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setFilename(document.getFilename());
        dto.setContentType(document.getContentType());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setEncrypted(document.getEncrypted());
        dto.setCarId(document.getCar().getId());
        dto.setCarLicensePlate(document.getCar().getLicensePlate());
        dto.setFileSize(document.getContent() != null ? document.getContent().length : 0);
        return dto;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Document metadata without binary content")
    public static class DocumentDto {
        @Schema(description = "Unique identifier of the document")
        private Long id;
        
        @Schema(description = "Original filename", example = "vehicle-registration.pdf")
        private String filename;
        
        @Schema(description = "MIME type of the document", example = "application/pdf")
        private String contentType;
        
        @Schema(description = "Upload timestamp")
        private LocalDateTime uploadedAt;
        
        @Schema(description = "Whether the document is encrypted")
        private Boolean encrypted;
        
        @Schema(description = "ID of the associated car")
        private Long carId;
        
        @Schema(description = "License plate of the associated car", example = "AB-123-CD")
        private String carLicensePlate;
        
        @Schema(description = "File size in bytes")
        private Integer fileSize;
    }
}