package org.citycare.emergencyservice.controller;

import org.citycare.emergencyservice.dto.request.AmbulanceRequest;
import org.citycare.emergencyservice.dto.response.ApiResponse;
import org.citycare.emergencyservice.dto.request.DispatchRequest;
import org.citycare.emergencyservice.dto.request.EmergencyRequest;
import org.citycare.emergencyservice.dto.response.EmergencyResponse;
import org.citycare.emergencyservice.entity.Ambulance;
import org.citycare.emergencyservice.entity.Emergency;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.citycare.emergencyservice.services.EmergencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/report")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Emergency>> report(
            @Valid @RequestBody EmergencyRequest request) {

        // Request nundi citizenId pampalsina pani ledu, context nundi tiskuntundi
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Emergency reported. Help is on the way.",
                        emergencyService.reportEmergency(request)));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<Emergency>>> getAllEmergencies() {
        return ResponseEntity.ok(ApiResponse.ok("All emergencies", emergencyService.getAllEmergencies()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<List<Emergency>>> myEmergencies() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long citizenId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok("My emergencies", emergencyService.getMyCases(citizenId)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Emergency>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending emergencies", emergencyService.getReportedEmergencies()));
    }

    @GetMapping("/ambulances/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Ambulance>>> getAvailableAmbulances() {
        return ResponseEntity.ok(ApiResponse.ok("Available ambulances", emergencyService.getAvailableAmbulances()));
    }

    @PostMapping("/{id}/dispatch")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<Emergency>> dispatch(
            @PathVariable Long id,
            @Valid @RequestBody DispatchRequest request,
            Authentication authentication) {
        Long dispatcherId;
        try {
            dispatcherId = Long.parseLong(authentication.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
        }
        return ResponseEntity.ok(ApiResponse.ok("Ambulance dispatched",
                emergencyService.dispatchAmbulance(id, dispatcherId, request)));
    }

    @GetMapping("/dispatched")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Emergency>>> getDispatched() {
        return ResponseEntity.ok(ApiResponse.ok("Dispatched emergencies", emergencyService.getDispatchedEmergencies()));
    }

    @GetMapping("/my-dispatch-history")
    @PreAuthorize("hasRole('DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Emergency>>> getMyDispatchHistory(Authentication authentication) {
        Long dispatcherId;
        try {
            dispatcherId = Long.parseLong(authentication.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
        }
        return ResponseEntity.ok(ApiResponse.ok("My dispatch history",
                emergencyService.getMyDispatchHistory(dispatcherId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Emergency>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Emergency", emergencyService.getById(id)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<Emergency>> updateStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Emergency status updated to " + status,
                emergencyService.updateEmergencyStatus(id, status)));
    }

    @PutMapping("/{emergencyId}/release-ambulance")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<String>> releaseAmbulance(@PathVariable Long emergencyId) {
        emergencyService.releaseAmbulanceForEmergency(emergencyId);
        return ResponseEntity.ok(ApiResponse.ok("Ambulance released successfully", null));
    }

    // ── Admin: Ambulance management ───────────────────────────────────────────

    @PostMapping("/admin/ambulances")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Ambulance>> addAmbulance(@Valid @RequestBody AmbulanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ambulance added", emergencyService.addAmbulance(request)));
    }

    @GetMapping("/admin/ambulances")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<List<Ambulance>>> getAllAmbulances() {
        return ResponseEntity.ok(ApiResponse.ok("All ambulances", emergencyService.getAllAmbulances()));
    }

    @PatchMapping("/admin/ambulances/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Ambulance>> updateAmbulanceStatus(
            @PathVariable Long id, @RequestParam Ambulance.Status status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + status,
                emergencyService.updateAmbulanceStatus(id, status)));
    }

    @DeleteMapping("/admin/ambulances/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAmbulance(@PathVariable Long id) {
        emergencyService.deleteAmbulance(id);
        return ResponseEntity.ok(ApiResponse.ok("Ambulance deleted successfully", null));
    }

    @GetMapping("/internal/{id}")
    @PreAuthorize("isAuthenticated()")
    public EmergencyResponse getByIdInternal(@PathVariable Long id) {
        return emergencyService.getEmergencyResponseById(id);
    }

}