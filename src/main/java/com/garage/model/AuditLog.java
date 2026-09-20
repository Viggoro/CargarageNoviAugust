package com.garage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee performedBy;
    
    @Column(nullable = false)
    private String entity;
    
    private Long entityId;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    private String details;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}