package org.citycare.citizenservice.serviceImplementation;

import org.citycare.citizenservice.dto.request.CitizenProfileRequest;
import org.citycare.citizenservice.dto.request.UserProfileUpdateRequest;
import org.citycare.citizenservice.dto.response.CitizenDocumentResponse;
import org.citycare.citizenservice.dto.response.CitizenResponse;
import org.citycare.citizenservice.entity.Citizen;
import org.citycare.citizenservice.entity.CitizenDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.citizenservice.exception.ResourceNotFoundException;
import org.citycare.citizenservice.feign.AuthClient;
import org.citycare.citizenservice.repository.CitizenDocumentRepository;
import org.citycare.citizenservice.repository.CitizenRepository;
import org.citycare.citizenservice.service.CitizenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenServiceImplementation implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final CitizenDocumentRepository documentRepository;
    private final AuthClient authClient;

    @Transactional
    public Citizen createCitizenFromRegistration(Long userId, String name, String contactInfo) {
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

    @Transactional
    public Citizen createOrUpdateProfile(Long userId, CitizenProfileRequest req) {
        log.info("Updating profile for userId={}, request={}", userId, req);
        Citizen citizen = citizenRepository.findById(userId).orElse(new Citizen());
        citizen.setCitizenId(userId);

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
        if (req.hasField("dateOfBirth")) citizen.setDateOfBirth(req.getDateOfBirth());
        if (req.hasField("gender")) citizen.setGender(req.getGender());
        if (req.hasField("address")) citizen.setAddress(req.getAddress() == null || req.getAddress().isBlank() ? null : req.getAddress().trim());

        if (citizen.getName() == null || citizen.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        citizen.setStatus(Citizen.Status.ACTIVE);
        Citizen saved = citizenRepository.save(citizen);

        try {
            if (req.hasField("name") || req.hasField("contactInfo")) {
                authClient.updateUserProfile(userId, new UserProfileUpdateRequest(saved.getName(), saved.getContactInfo()));
            }
        } catch (Exception e) {
            log.warn("Failed to update auth service profile: {}", e.getMessage());
        }
        return saved;
    }

    public Citizen getProfile(Long userId) {
        return citizenRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found", userId));
    }

    public Citizen getById(Long citizenId) {
        return citizenRepository.findById(citizenId).orElseThrow(() -> new ResourceNotFoundException("Citizen", citizenId));
    }

    @Override
    public CitizenResponse getCitizenResponseById(Long citizenId) {
        Citizen citizen = getById(citizenId);
        return CitizenResponse.builder()
                .citizenId(citizen.getCitizenId())
                .name(citizen.getName())
                .contactInfo(citizen.getContactInfo())
                .status("ACTIVE")
                .build();
    }

    @Override
    public CitizenResponse getCitizenResponseByUserId(Long userId) {
        return getCitizenResponseById(userId);
    }
    public Page<Citizen> getAll(Pageable pageable) {
        return citizenRepository.findAll(pageable);
    }

    @Transactional
    public CitizenDocument uploadDocument(Long citizenId, byte[] documentData, String contentType) {
        Citizen citizen = getById(citizenId);
        CitizenDocument doc = CitizenDocument.builder()
                .citizen(citizen)
                .documentData(documentData)
                .contentType(contentType)
                .uploadedDate(LocalDate.now())
                .verificationStatus(CitizenDocument.VerificationStatus.PENDING)
                .build();
        return documentRepository.save(doc);
    }

    public CitizenDocument getDocumentWithBlob(Long documentId) {
        return documentRepository.findById(documentId).orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    @Transactional
    public CitizenDocumentResponse verifyDocument(Long documentId, CitizenDocument.VerificationStatus status) {
        CitizenDocument doc = documentRepository.findById(documentId).orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        doc.setVerificationStatus(status);
        CitizenDocument saved = documentRepository.save(doc);
        return CitizenDocumentResponse.builder()
                .documentId(saved.getDocumentId())
                .verificationStatus(saved.getVerificationStatus().name())
                .uploadedDate(saved.getUploadedDate())
                .build();
    }

    public List<CitizenDocumentResponse> getDocuments(Long citizenId) {
        return documentRepository.findByCitizenCitizenId(citizenId).stream()
                .map(doc -> CitizenDocumentResponse.builder()
                        .documentId(doc.getDocumentId())
                        .verificationStatus(doc.getVerificationStatus().name())
                        .uploadedDate(doc.getUploadedDate())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCitizenDocumentVerified(Long citizenId) {
        List<CitizenDocument> docs = documentRepository.findByCitizenCitizenId(citizenId);
        boolean isVerified = docs.stream().anyMatch(d -> d.getVerificationStatus() == CitizenDocument.VerificationStatus.VERIFIED);
        log.info("Checking verification for citizen {}: found {} documents, verified={}", citizenId, docs.size(), isVerified);
        return isVerified;
    }
}