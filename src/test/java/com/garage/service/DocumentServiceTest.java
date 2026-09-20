package com.garage.service;

import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.model.BackOfficeEmployee;
import com.garage.model.Car;
import com.garage.model.Customer;
import com.garage.model.Document;
import com.garage.model.enums.Role;
import com.garage.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Document Service Tests")
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CarService carService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DocumentService documentService;

    private Car testCar;
    private BackOfficeEmployee testEmployee;
    private Document testDocument;

    @BeforeEach
    void setUp() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");

        testCar = new Car();
        testCar.setId(1L);
        testCar.setLicensePlate("AB-123-CD");
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setYear(2020);
        testCar.setOwner(customer);

        testEmployee = new BackOfficeEmployee();
        testEmployee.setId(1L);
        testEmployee.setUsername("admin");
        testEmployee.setEmail("admin@garage.com");
        testEmployee.setName("Admin User");
        testEmployee.setRole(Role.BACK_OFFICE);

        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setFilename("test-document.pdf");
        testDocument.setContentType("application/pdf");
        testDocument.setContent("PDF content".getBytes());
        testDocument.setCar(testCar);
        testDocument.setUploadedAt(LocalDateTime.now());
        testDocument.setEncrypted(false);
    }

    @Test
    @DisplayName("Should successfully upload a valid PDF document")
    void testUploadDocument_Success() {
        byte[] content = "PDF file content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vehicle-registration.pdf",
                "application/pdf",
                content
        );

        when(carService.findById(1L)).thenReturn(testCar);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        Document result = documentService.uploadDocument(1L, file, testEmployee);

        assertNotNull(result);
        assertEquals("test-document.pdf", result.getFilename());
        assertEquals("application/pdf", result.getContentType());
        verify(carService, times(1)).findById(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(auditLogService, times(1)).log(
                eq("UPLOAD"),
                eq(testEmployee),
                eq("Document"),
                any(Long.class),
                anyString()
        );
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void testUploadDocument_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> documentService.uploadDocument(1L, emptyFile, testEmployee)
        );
        assertTrue(exception.getMessage().contains("empty"));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Should throw exception when file is not a PDF")
    void testUploadDocument_InvalidContentType() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "Some text content".getBytes()
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> documentService.uploadDocument(1L, invalidFile, testEmployee)
        );
        assertTrue(exception.getMessage().contains("PDF"));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Should throw exception when file is null")
    void testUploadDocument_NullFile() {
        assertThrows(
                BadRequestException.class,
                () -> documentService.uploadDocument(1L, null, testEmployee)
        );
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Should throw exception when file size exceeds maximum")
    void testUploadDocument_FileTooLarge() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-document.pdf",
                "application/pdf",
                largeContent
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> documentService.uploadDocument(1L, largeFile, testEmployee)
        );
        assertTrue(exception.getMessage().contains("exceeds maximum"));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Should throw exception when file doesn't have .pdf extension")
    void testUploadDocument_InvalidExtension() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "document.doc",
                "application/pdf",
                "PDF content".getBytes()
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> documentService.uploadDocument(1L, invalidFile, testEmployee)
        );
        assertTrue(exception.getMessage().contains("pdf extension"));
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    @DisplayName("Should find document by ID")
    void testFindById_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        Document result = documentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test-document.pdf", result.getFilename());
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when document not found")
    void testFindById_NotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.findById(999L)
        );
    }

    @Test
    @DisplayName("Should find documents by car ID")
    void testFindByCarId_Success() {
        List<Document> documents = Arrays.asList(testDocument);
        when(carService.findById(1L)).thenReturn(testCar);
        when(documentRepository.findByCarId(1L)).thenReturn(documents);

        List<Document> result = documentService.findByCarId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-document.pdf", result.get(0).getFilename());
        verify(carService, times(1)).findById(1L);
        verify(documentRepository, times(1)).findByCarId(1L);
    }

    @Test
    @DisplayName("Should download document content")
    void testDownloadDocument_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        byte[] result = documentService.downloadDocument(1L);

        assertNotNull(result);
        assertArrayEquals("PDF content".getBytes(), result);
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should delete document successfully")
    void testDeleteDocument_Success() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        doNothing().when(documentRepository).delete(any(Document.class));

        documentService.deleteDocument(1L, testEmployee);

        verify(documentRepository, times(1)).findById(1L);
        verify(documentRepository, times(1)).delete(testDocument);
        verify(auditLogService, times(1)).log(
                eq("DELETE"),
                eq(testEmployee),
                eq("Document"),
                eq(1L),
                anyString()
        );
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent document")
    void testDeleteDocument_NotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.deleteDocument(999L, testEmployee)
        );
        verify(documentRepository, never()).delete(any(Document.class));
    }
}
