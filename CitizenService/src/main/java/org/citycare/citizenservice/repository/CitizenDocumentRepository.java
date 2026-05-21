package org.citycare.citizenservice.repository;

import org.citycare.citizenservice.entity.CitizenDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CitizenDocumentRepository extends JpaRepository<CitizenDocument, Long> {
    List<CitizenDocument> findByCitizenCitizenId(Long citizenId);
    boolean existsByCitizenCitizenIdAndVerificationStatus(Long citizenId, CitizenDocument.VerificationStatus status);
}

