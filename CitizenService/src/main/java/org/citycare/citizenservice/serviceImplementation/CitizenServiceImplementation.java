package org.citycare.citizenservice.serviceImplementation;

import org.citycare.citizenservice.dto.request.CitizenProfileRequest;
import org.citycare.citizenservice.dto.request.UserProfileUpdateRequest;
import org.citycare.citizenservice.dto.response.CitizenDocumentResponse;
import org.citycare.citizenservice.dto.response.CitizenResponse;
import org.citycare.citizenservice.entity.Citizen;
import org.citycare.citizenservice.entity.CitizenDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.citizenservice.repository.CitizenDocumentRepository;
import org.citycare.citizenservice.repository.CitizenRepository;
import org.citycare.citizenservice.service.CitizenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenServiceImplementation implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final CitizenDocumentRepository documentRepository;
    private final AuthClient authClient;

    // ── Called by auth-service via OpenFeign on citizen registration ──────────

    /**
     * Auto-creates a minimal citizen profile when a new user registers.
     * Called internally by auth-service via OpenFeign — not by the user directly.
     * If a profile already exists for this userId (e.g. idempotency), it is returned as-is.
     */
    @Transactional
    public Citizen createCitizenFromRegistration(Long userId, String name, String contactInfo) {
        // Idempotent: if profile already exists, return it
        return citizenRepository.findById(userId).orElseGet(() -> {
            log.info("Auto-creating citizen profile for userId={}, name={}", userId, name);
            Citizen citizen = Citizen.builder()
                    .citizenId(userId)
                    .name(name)
                    .contactInfo(contactInfo)
                    .status(Citizen.Status.ACTIVE)
                    .build();
            return citizenRepository.save(citizen);
        });
    }

    // ── Standard CRUD ─────────────────────────────────────────────────────────

    @Transactional
    public Citizen createOrUpdateProfile(Long userId, CitizenProfileRequest req) {

        log.info("Updating profile for userId={}, request={}", userId, req);

        Citizen citizen = citizenRepository.findById(userId)
                .orElse(new Citizen());

        citizen.setCitizenId(userId);

        // Required fields: explicit null/blank is not allowed
        if (req.hasField("name")) {
            if (req.getName() == null || req.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty");
            }
            citizen.setName(req.getName().trim());
        }

        if (req.hasField("contactInfo")) {
            if (req.getContactInfo() == null || req.getContactInfo().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact info cannot be empty");
            }
            citizen.setContactInfo(req.getContactInfo().trim());
        }

        // Optional fields: explicit null/blank clears value, omitted means no change
        if (req.hasField("dateOfBirth")) {
            citizen.setDateOfBirth(req.getDateOfBirth());
        }

        if (req.hasField("gender")) {
            citizen.setGender(req.getGender());
        }

        if (req.hasField("address")) {
            if (req.getAddress() == null || req.getAddress().isBlank()) {
                citizen.setAddress(null);
            } else {
                citizen.setAddress(req.getAddress().trim());
            }
        }

        if (citizen.getName() == null || citizen.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        citizen.setStatus(Citizen.Status.ACTIVE);

        // Save citizen first
        Citizen saved = citizenRepository.save(citizen);
        log.info("Citizen saved successfully: {}", saved.getCitizenId());

        // Try to update auth service, but don't fail if it errors
        try {
            if (req.hasField("name") || req.hasField("contactInfo")) {
                authClient.updateUserProfile(
                        userId,
                        new UserProfileUpdateRequest(
                                saved.getName(),
                                saved.getContactInfo()
                        )
                );
                log.info("Auth service profile updated successfully");
            }
        } catch (Exception e) {
            log.warn("Failed to update auth service profile, but citizen profile saved: {}", e.getMessage());
        }

        return saved;
    }

    public Citizen getProfile(Long userId) {
        return citizenRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found. Please complete your profile.", userId));
    }

    public Citizen getById(Long citizenId) {
        return citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", citizenId));
    }


    @Override
    public CitizenResponse getCitizenResponseById(Long citizenId) {

        Citizen citizen = citizenRepository.getById(citizenId);

        return CitizenResponse.builder()
                .citizenId(citizen.getCitizenId())
                .name(citizen.getName())
                .contactInfo(citizen.getContactInfo())
                .status("ACTIVE")
                .build();

    }

    @Override
    public CitizenResponse getCitizenResponseByUserId(Long userId) {
        // citizenId == userId in this system
        return getCitizenResponseById(userId);
    }
    public List<Citizen> getAll() {
        return citizenRepository.findAll();
    }

    @Transactional
    public CitizenDocument uploadDocument(Long citizenId, byte[] documentData, String contentType) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", citizenId));

        CitizenDocument doc = CitizenDocument.builder()
                .citizen(citizen)
                .documentData(documentData)
                .contentType(contentType)
                .uploadedDate(LocalDate.now())
                .verificationStatus(CitizenDocument.VerificationStatus.PENDING)
                .build();

        CitizenDocument saved = documentRepository.save(doc);

        return saved;
    }

    public CitizenDocument getDocumentWithBlob(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    @Transactional
    public CitizenDocumentResponse verifyDocument(Long documentId,
                                                  CitizenDocument.VerificationStatus status) {

        CitizenDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));

        doc.setVerificationStatus(status);

        return CitizenDocumentResponse.builder()
                .documentId(doc.getDocumentId())
                .verificationStatus(doc.getVerificationStatus().name())
                .uploadedDate(doc.getUploadedDate())
                .build();
    }

    public List<CitizenDocumentResponse> getDocuments(Long citizenId) {

        List<CitizenDocument> documents =
                documentRepository.findByCitizenCitizenId(citizenId);

        return documents.stream()
                .map(doc -> CitizenDocumentResponse.builder()
                        .documentId(doc.getDocumentId())
                        .verificationStatus(doc.getVerificationStatus().name())
                        .uploadedDate(doc.getUploadedDate())
                        .build())
                .toList();
    }

    @Override
    public boolean isCitizenDocumentVerified(Long citizenId) {
        return documentRepository.existsByCitizenCitizenIdAndVerificationStatus(
                citizenId, CitizenDocument.VerificationStatus.VERIFIED);
    }
}
