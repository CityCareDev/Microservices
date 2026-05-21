package org.citycare.complianceservice.controller;

import org.citycare.complianceservice.dto.response.ApiResponse;
import org.citycare.complianceservice.dto.request.AuditRequest;
import org.citycare.complianceservice.dto.request.ComplianceRecordRequest;
import org.citycare.complianceservice.entity.Audit;
import org.citycare.complianceservice.entity.AuditLog;
import org.citycare.complianceservice.entity.ComplianceRecord;
import org.citycare.complianceservice.ServiceImplementation.ComplianceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceServiceImpl complianceServiceImpl;

    // ── Compliance Records ────────────────────────────────────────────────────

    @PostMapping("/records")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<ComplianceRecord>> createRecord(
            @Valid @RequestBody ComplianceRecordRequest request,
            Authentication authentication) {
        Long officerId = getAuthenticatedUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Compliance record created",
                        complianceServiceImpl.createRecord(officerId, request)));
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<ComplianceRecord>>> getAllRecords(Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("All compliance records", complianceServiceImpl.getAllRecords(actorId)));
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<ComplianceRecord>> getRecordById(@PathVariable Long id, Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Compliance record", complianceServiceImpl.getRecordById(actorId, id)));
    }

    @GetMapping("/records/entity/{entityId}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<ComplianceRecord>>> getByEntity(@PathVariable Long entityId, Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Records for entity " + entityId,
                complianceServiceImpl.getRecordsByEntity(actorId, entityId)));
    }

    @GetMapping("/records/type/{type}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<ComplianceRecord>>> getByType(
            @PathVariable ComplianceRecord.EntityType type,
            Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Records by type: " + type,
                complianceServiceImpl.getRecordsByType(actorId, type)));
    }

    // ── Audits ────────────────────────────────────────────────────────────────

    @PostMapping("/audits")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<Audit>> createAudit(
            @Valid @RequestBody AuditRequest request,
            Authentication authentication) {
        Long officerId = getAuthenticatedUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Audit created", complianceServiceImpl.createAudit(officerId, request)));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
        }
    }

    @GetMapping("/audits")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<Audit>>> getAllAudits(Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("All audits", complianceServiceImpl.getAllAudits(actorId)));
    }

    @GetMapping("/audits/{id}")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<Audit>> getAudit(@PathVariable Long id, Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Audit", complianceServiceImpl.getAuditById(actorId, id)));
    }

    @PatchMapping("/audits/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<Audit>> updateAuditStatus(
            @PathVariable Long id,
            @RequestParam Audit.Status status,
            @RequestParam(required = false) String findings,
            Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Audit updated",
                complianceServiceImpl.updateAuditStatus(actorId, id, status, findings)));
    }

    // ── Audit Logs ────────────────────────────────────────────────────────────

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLogs(Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        List<AuditLog> logs = isAdmin
                ? complianceServiceImpl.getAllLogs(actorId)
                : complianceServiceImpl.getLogsByUser(actorId, actorId);
        return ResponseEntity.ok(ApiResponse.ok("Audit logs", logs));
    }

    @GetMapping("/logs/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLogsByUser(@PathVariable Long userId, Authentication authentication) {
        Long actorId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Logs for user " + userId,
                complianceServiceImpl.getLogsByUser(actorId, userId)));
    }
}
