package com.garage.service;

import com.garage.model.AuditLog;
import com.garage.model.Employee;
import com.garage.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Transactional
    public AuditLog log(String action, Employee performedBy, String entity, Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setPerformedBy(performedBy);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        
        return auditLogRepository.save(auditLog);
    }
    
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
    
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
    
    public List<AuditLog> findByEmployee(Long employeeId) {
        return auditLogRepository.findByPerformedById(employeeId);
    }
    
    public List<AuditLog> findByEntity(String entity) {
        return auditLogRepository.findByEntity(entity);
    }
    
    public List<AuditLog> findByEntityAndId(String entity, Long entityId) {
        return auditLogRepository.findByEntityAndEntityId(entity, entityId);
    }
    
    public Page<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable);
    }
}