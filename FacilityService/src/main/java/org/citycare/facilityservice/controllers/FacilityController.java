package org.citycare.facilityservice.controllers;

import org.citycare.facilityservice.dto.request.FacilityRequest;
import org.citycare.facilityservice.dto.response.FacilityResponse;
import org.citycare.facilityservice.dto.response.StaffResponse;
import org.citycare.facilityservice.dto.response.ApiResponse;
import org.citycare.facilityservice.entities.Facility;
import org.citycare.facilityservice.services.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacilityResponse>> create(@Valid @RequestBody FacilityRequest request) {
        FacilityResponse data = facilityService.createFacility(request);
        return new ResponseEntity<>(ApiResponse.ok("Facility created successfully", data), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'DISPATCHER', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getAll() {
        List<FacilityResponse> data = facilityService.getAll();
        return ResponseEntity.ok(ApiResponse.ok("All facilities retrieved", data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FacilityResponse>> getById(@PathVariable Long id) {
        FacilityResponse data = facilityService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("Facility details retrieved", data));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacilityResponse>> update(@PathVariable Long id, @Valid @RequestBody FacilityRequest request) {
        FacilityResponse data = facilityService.updateFacility(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Facility updated successfully", data));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacilityResponse>> updateStatus(@PathVariable Long id, @RequestParam Facility.Status status) {
        FacilityResponse data = facilityService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Facility status updated to " + status, data));
    }

    @GetMapping("/{id}/staff")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getStaff(@PathVariable Long id) {
        List<StaffResponse> data = facilityService.getStaffByFacility(id);
        return ResponseEntity.ok(ApiResponse.ok("Staff members for facility retrieved", data));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getByType(@PathVariable Facility.Type type) {
        List<FacilityResponse> data = facilityService.getByType(type);
        return ResponseEntity.ok(ApiResponse.ok("Facilities filtered by type: " + type, data));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getByStatus(@PathVariable Facility.Status status) {
        List<FacilityResponse> data = facilityService.getByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok("Facilities filtered by status: " + status, data));
    }
}