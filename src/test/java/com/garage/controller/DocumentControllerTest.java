package com.garage.controller;

import com.garage.model.BackOfficeEmployee;
import com.garage.model.Car;
import com.garage.model.Customer;
import com.garage.model.Document;
import com.garage.model.enums.Role;
import com.garage.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Document Controller Integration Tests")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
        testEmployee.setIsActive(true);

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
    @DisplayName("Should get documents by car ID")
    @WithMockUser(roles = "BACK_OFFICE")
    void testGetDocumentsByCarId() throws Exception {
        List<Document> documents = Arrays.asList(testDocument);
        when(documentService.findByCarId(1L)).thenReturn(documents);

        mockMvc.perform(get("/api/documents/car/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].filename").value("test-document.pdf"))
                .andExpect(jsonPath("$[0].contentType").value("application/pdf"))
                .andExpect(jsonPath("$[0].carLicensePlate").value("AB-123-CD"));

        verify(documentService, times(1)).findByCarId(1L);
    }

    @Test
    @DisplayName("Should get document by ID")
    @WithMockUser(roles = "BACK_OFFICE")
    void testGetDocumentById() throws Exception {
        when(documentService.findById(1L)).thenReturn(testDocument);

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.filename").value("test-document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.carLicensePlate").value("AB-123-CD"));

        verify(documentService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should download document")
    @WithMockUser(roles = "BACK_OFFICE")
    void testDownloadDocument() throws Exception {
        byte[] content = "PDF file content".getBytes();
        when(documentService.findById(1L)).thenReturn(testDocument);
        when(documentService.downloadDocument(1L)).thenReturn(content);

        mockMvc.perform(get("/api/documents/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"test-document.pdf\""))
                .andExpect(content().bytes(content));

        verify(documentService, times(1)).findById(1L);
        verify(documentService, times(1)).downloadDocument(1L);
    }

    @Test
    @DisplayName("Should upload document with BACK_OFFICE role")
    @WithMockUser(roles = "BACK_OFFICE")
    void testUploadDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vehicle-registration.pdf",
                "application/pdf",
                "PDF file content".getBytes()
        );

        when(documentService.uploadDocument(eq(1L), any(), any())).thenReturn(testDocument);

        mockMvc.perform(multipart("/api/documents/car/1/upload")
                        .file(file)
                        .with(user(testEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("test-document.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"));

        verify(documentService, times(1)).uploadDocument(eq(1L), any(), any());
    }

    @Test
    @DisplayName("Should deny upload without BACK_OFFICE role")
    @WithMockUser(roles = "MECHANIC")
    void testUploadDocument_Forbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vehicle-registration.pdf",
                "application/pdf",
                "PDF file content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/car/1/upload")
                        .file(file))
                .andExpect(status().isForbidden());

        verify(documentService, never()).uploadDocument(any(), any(), any());
    }

    @Test
    @DisplayName("Should deny upload without authentication")
    void testUploadDocument_Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vehicle-registration.pdf",
                "application/pdf",
                "PDF file content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/car/1/upload")
                        .file(file))
                .andExpect(status().isForbidden());

        verify(documentService, never()).uploadDocument(any(), any(), any());
    }

    @Test
    @DisplayName("Should delete document with BACK_OFFICE role")
    @WithMockUser(roles = "BACK_OFFICE")
    void testDeleteDocument_Success() throws Exception {
        doNothing().when(documentService).deleteDocument(eq(1L), any());

        mockMvc.perform(delete("/api/documents/1")
                        .with(user(testEmployee)))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).deleteDocument(eq(1L), any());
    }

    @Test
    @DisplayName("Should deny delete without BACK_OFFICE role")
    @WithMockUser(roles = "MECHANIC")
    void testDeleteDocument_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isForbidden());

        verify(documentService, never()).deleteDocument(any(), any());
    }

    @Test
    @DisplayName("Should allow authenticated users to view documents")
    @WithMockUser(roles = "MECHANIC")
    void testGetDocuments_Authenticated() throws Exception {
        List<Document> documents = Arrays.asList(testDocument);
        when(documentService.findByCarId(1L)).thenReturn(documents);

        mockMvc.perform(get("/api/documents/car/1"))
                .andExpect(status().isOk());

        verify(documentService, times(1)).findByCarId(1L);
    }
}
