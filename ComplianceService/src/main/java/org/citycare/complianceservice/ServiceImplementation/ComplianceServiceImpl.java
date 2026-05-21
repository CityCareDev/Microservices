package org.citycare.complianceservice.ServiceImplementation;

import org.citycare.complianceservice.dto.request.AuditRequest;
import org.citycare.complianceservice.dto.request.ComplianceRecordRequest;
import org.citycare.complianceservice.entity.Audit;
import org.citycare.complianceservice.entity.AuditLog;
import org.citycare.complianceservice.entity.ComplianceRecord;
import org.citycare.complianceservice.exception.ResourceNotFoundException;
import org.citycare.complianceservice.feign.EmergencyClient;
import org.citycare.complianceservice.feign.FacilityClient;
import org.citycare.complianceservice.feign.PatientClient;
import org.citycare.complianceservice.repository.AuditLogRepository;
import org.citycare.complianceservice.repository.AuditRepository;
import org.citycare.complianceservice.repository.ComplianceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceServiceImpl implements org.citycare.complianceservice.service.ComplianceService {

    private final ComplianceRecordRepository recordRepository;
    private final AuditRepository auditRepository;
    private final AuditLogRepository auditLogRepository;

    // OpenFeign clients for cross-service entity validation
    private final FacilityClient facilityClient;
    private final PatientClient patientClient;
    private final EmergencyClient emergencyClient;

    // ── Compliance Records ────────────────────────────────────────────────────

    @Transactional
    public ComplianceRecord createRecord(Long officerId, ComplianceRecordRequest req) {
        // Validate the target entity exists in the respective service via OpenFeign
        validateEntity(req.getType(), req.getEntityId());

        ComplianceRecord record = ComplianceRecord.builder()
                .entityId(req.getEntityId())
                .type(req.getType())
                .result(req.getResult())
                .date(req.getDate())
                .notes(req.getNotes())
                .officerId(officerId)
                .build();
        ComplianceRecord saved = recordRepository.save(record);
        logAction(officerId, "CREATE_COMPLIANCE_RECORD", "compliance_records/" + saved.getComplianceId());
        return saved;
    }

    public List<ComplianceRecord> getAllRecords(Long actorId) {
        List<ComplianceRecord> records = recordRepository.findAll();
        return records;
    }

    public ComplianceRecord getRecordById(Long actorId, Long id) {
        Objects.requireNonNull(id, "id must not be null");
        ComplianceRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", id));
        return record;
    }

    public List<ComplianceRecord> getRecordsByEntity(Long actorId, Long entityId) {
        List<ComplianceRecord> records = recordRepository.findByEntityId(entityId);
        return records;
    }

    public List<ComplianceRecord> getRecordsByType(Long actorId, ComplianceRecord.EntityType type) {
        List<ComplianceRecord> records = recordRepository.findByType(type);
        return records;
    }

    // ── Audits ────────────────────────────────────────────────────────────────

    @Transactional
    public Audit createAudit(Long officerId, AuditRequest req) {
        Audit audit = Audit.builder()
                .officerId(officerId)
                .scope(req.getScope())
                .findings(req.getFindings())
                .date(req.getDate())
                .status(Audit.Status.SCHEDULED)
                .build();
        Audit saved = auditRepository.save(audit);
        logAction(officerId, "CREATE_AUDIT", "audits/" + saved.getAuditId());
        return saved;
    }

    public List<Audit> getAllAudits(Long actorId) {
        List<Audit> audits = auditRepository.findAll();
        return audits;
    }

    public Audit getAuditById(Long actorId, Long id) {
        Objects.requireNonNull(id, "id must not be null");
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit", id));
        return audit;
    }

    @Transactional
    public Audit updateAuditStatus(Long actorId, Long id, Audit.Status status, String findings) {
        Objects.requireNonNull(id, "id must not be null");
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit", id));
        audit.setStatus(status);
        if (findings != null) audit.setFindings(findings);
        Audit saved = Objects.requireNonNull(auditRepository.save(audit));
        logAction(actorId, "UPDATE_AUDIT_STATUS", "audits/" + id + "?status=" + status.name());
        return saved;
    }

    // ── Audit Logs ────────────────────────────────────────────────────────────

    public List<AuditLog> getAllLogs(Long actorId) {
        List<AuditLog> logs = auditLogRepository.findAll();
        return logs;
    }

    public List<AuditLog> getLogsByUser(Long actorId, Long userId) {
        List<AuditLog> logs = auditLogRepository.findByUserId(userId);
        return logs;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Validates the target entity (FACILITY / PATIENT / EMERGENCY) exists
     * in the responsible microservice via OpenFeign before creating a compliance record.
     */
    private void validateEntity(ComplianceRecord.EntityType type, Long entityId) {
        try {
            switch (type) {
                case FACILITY -> {
                    var facility = facilityClient.getFacilityById(entityId);
                    log.info("Compliance record targeting FACILITY id={} name={}", entityId, facility.getName());
                }
                case PATIENT -> {
                    var patient = patientClient.getPatientById(entityId);
                    log.info("Compliance record targeting PATIENT id={} status={}", entityId, patient.getStatus());
                }
                case EMERGENCY -> {
                    var emergency = emergencyClient.getEmergencyById(entityId);
                    log.info("Compliance record targeting EMERGENCY id={} status={}", entityId, emergency.getStatus());
                }
            }
        } catch (Exception e) {
            log.warn("Could not validate {} entityId={} via OpenFeign: {}", type, entityId, e.getMessage());
            // Non-fatal: allow creation even if remote service is down
        }
    }

    private void logAction(Long userId, String action, String resource) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
