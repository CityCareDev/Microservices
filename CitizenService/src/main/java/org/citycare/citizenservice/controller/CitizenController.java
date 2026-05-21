package org.citycare.citizenservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.citycare.citizenservice.dto.request.CitizenProfileRequest;
import org.citycare.citizenservice.dto.response.ApiResponse;
import org.citycare.citizenservice.dto.response.CitizenDocumentResponse;
import org.citycare.citizenservice.dto.response.CitizenResponse;
import org.citycare.citizenservice.entity.Citizen;
import org.citycare.citizenservice.entity.CitizenDocument;
import org.citycare.citizenservice.service.CitizenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/citizens")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenService citizenService;

    // ── User Profile Management ────────────────────────────────────────────────

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Citizen>> updateProfile(
            @Valid @RequestBody CitizenProfileRequest request) {
        Long userId = getAuthenticatedUserId();
        // User can only update their own profile (admins can update anyone's via admin endpoint)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Profile saved", citizenService.createOrUpdateProfile(userId, request)));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Citizen>> getMyProfile() {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(ApiResponse.ok("Citizen profile", citizenService.getProfile(userId)));
    }

    // ── ADMIN ENDPOINTS ────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Citizen>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All citizens (ADMIN ONLY)", citizenService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Citizen>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Citizen details", citizenService.getById(id)));
    }

    // ── Document Management ────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/documents")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<CitizenDocument>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = getAuthenticatedUserId();
        // Verify user is uploading for themselves or is admin
        if (!id.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only upload documents for your own profile"));
        }
        CitizenDocument doc = citizenService.uploadDocument(id, file.getBytes(), file.getContentType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document uploaded", doc));
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CitizenDocumentResponse>>> getDocuments(
            @PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        // Verify user can access these documents (admins can access any)
        if (!id.equals(userId) && !isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only view your own documents"));
        }
        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Documents",
                        citizenService.getDocuments(id)
                )
        );
    }

    @GetMapping("/{id}/documents/{docId}/download")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        Long userId = getAuthenticatedUserId();

        // Verify user can download this document (admins can access any)
        if (!id.equals(userId) && !isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CitizenDocument doc = citizenService.getDocumentWithBlob(docId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (doc.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(doc.getContentType());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(doc.getDocumentData());
    }

    @PatchMapping("/documents/{docId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CitizenDocumentResponse>> verifyDocument(
            @PathVariable Long docId,
            @RequestParam CitizenDocument.VerificationStatus status) {

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "Document status updated to " + status,
                        citizenService.verifyDocument(docId, status)
                )
        );
    }

    // ── INTERNAL ENDPOINTS (for inter-service Feign calls) ────────────────────

    @GetMapping("/internal/{id}")
    @PreAuthorize("isAuthenticated()")
    public CitizenResponse getByIdInternal(@PathVariable Long id) {
        return citizenService.getCitizenResponseById(id);
    }

    @GetMapping("/internal/{id}/verified")
    @PreAuthorize("isAuthenticated()")
    public boolean isCitizenVerified(@PathVariable Long id) {
        return citizenService.isCitizenDocumentVerified(id);
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    private Long getAuthenticatedUserId() {
        try {
            return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
        }
    }

}
