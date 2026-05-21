package org.citycare.complianceservice.service;

import org.citycare.complianceservice.dto.request.AuditRequest;
import org.citycare.complianceservice.dto.request.ComplianceRecordRequest;
import org.citycare.complianceservice.entity.Audit;
import org.citycare.complianceservice.entity.AuditLog;
import org.citycare.complianceservice.entity.ComplianceRecord;

import java.util.List;

public interface ComplianceService {

    ComplianceRecord createRecord(Long officerId, ComplianceRecordRequest req);

    List<ComplianceRecord> getAllRecords(Long actorId);

    ComplianceRecord getRecordById(Long actorId, Long id);

    List<ComplianceRecord> getRecordsByEntity(Long actorId, Long entityId);

    List<ComplianceRecord> getRecordsByType(Long actorId, ComplianceRecord.EntityType type);

    Audit createAudit(Long officerId, AuditRequest req);

    List<Audit> getAllAudits(Long actorId);

    Audit getAuditById(Long actorId, Long id);

    Audit updateAuditStatus(Long actorId, Long id, Audit.Status status, String findings);

    List<AuditLog> getAllLogs(Long actorId);

    List<AuditLog> getLogsByUser(Long actorId, Long userId);
}