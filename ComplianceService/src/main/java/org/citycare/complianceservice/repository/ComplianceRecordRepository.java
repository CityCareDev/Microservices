package org.citycare.complianceservice.repository;

import org.citycare.complianceservice.entity.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {
    List<ComplianceRecord> findByEntityId(Long entityId);
    List<ComplianceRecord> findByType(ComplianceRecord.EntityType type);
}
