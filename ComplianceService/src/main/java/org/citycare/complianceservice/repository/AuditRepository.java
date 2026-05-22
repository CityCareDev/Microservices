package org.citycare.complianceservice.repository;

import org.citycare.complianceservice.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {}
