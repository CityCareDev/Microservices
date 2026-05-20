package org.citycare.patienttreatmentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.citycare.patienttreatmentservice.dto.AdmitPatientRequest;
import org.citycare.patienttreatmentservice.dto.ApiResponse;
import org.citycare.patienttreatmentservice.dto.TreatmentRequest;
import org.citycare.patienttreatmentservice.entity.Patient;
import org.citycare.patienttreatmentservice.entity.Treatment;
import org.citycare.patienttreatmentservice.feign.dto.EmergencyResponse;
import org.citycare.patienttreatmentservice.feign.dto.TreatmentSummaryResponse;
import org.citycare.patienttreatmentservice.serviceImplementation.PatientTreatmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Added
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PatientTreatmentController {

    private final PatientTreatmentService service;

    // ------------------------------------------- Patients ------------------------------------------------

    @PostMapping("/patients/admit")
    @PreAuthorize("hasRole('ADMIN')") // Only Admin can admit
    public ResponseEntity<ApiResponse<Patient>> admit(@Valid @RequestBody AdmitPatientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Patient admitted", service.admitPatient(req)));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<ApiResponse<List<Patient>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All patients", service.getAllPatients()));
    }

    @GetMapping("/patients/facility/{facilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Patient>>> getByFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Patients for facility " + facilityId,
                service.getPatientsByFacility(facilityId)));
    }

    @GetMapping("/patients/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Patient>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Patient", service.getPatientById(id)));
    }

    @PatchMapping("/patients/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Patient>> updateStatus(
            @PathVariable Long id, @RequestParam Patient.Status status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + status,
                service.updatePatientStatus(id, status)));
    }

    @GetMapping("/patients/{id}/emergency")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<EmergencyResponse>> getEmergencyForPatient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Emergency info for patient " + id,
                service.getEmergencyForPatient(id)));
    }

    @GetMapping("/patients/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Patient>>> getByStatus(@PathVariable Patient.Status status) {
        List<Patient> patients = service.getPatientsByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok("Patients with status " + status, patients));
    }

    @GetMapping("/patients/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Patient>>> getUnassigned() {
        return ResponseEntity.ok(ApiResponse.ok("Unassigned patients", service.getUnassignedPatients()));
    }

    @GetMapping("/patients/facility/{facilityId}/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Patient>>> getUnassignedByFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Unassigned patients for facility " + facilityId,
                service.getUnassignedPatientsByFacility(facilityId)));
    }

    @GetMapping("/patients/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Patient>>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.ok("Patients for doctor " + doctorId,
                service.getPatientsByDoctor(doctorId)));
    }

    @GetMapping("/patients/facility/{facilityId}/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Patient>>> getByFacilityAndDoctor(
            @PathVariable Long facilityId, @PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.ok("Doctor's patients in facility",
                service.getPatientsByFacilityAndDoctor(facilityId, doctorId)));
    }

    // ------------------------------    Treatments -------------------------------------------

    @PostMapping("/treatments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<Treatment>> addTreatment(
            @Valid @RequestBody TreatmentRequest req)
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Treatment added", service.addTreatment(req)));
    }

    @GetMapping("/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Treatment>>> getAllTreatments() {
        return ResponseEntity.ok(ApiResponse.ok("All treatments", service.getAllTreatments()));
    }

    @GetMapping("/treatments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Treatment>> getTreatmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Treatment", service.getTreatmentById(id)));
    }

    @GetMapping("/patients/{id}/treatments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<Treatment>>> getByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Treatments for patient " + id,
                service.getTreatmentsByPatient(id)));
    }

    @PatchMapping("/treatments/{id}/{status}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<Treatment>> updateTreatmentStatus(
            @PathVariable Long id, @PathVariable Treatment.Status status) {
        return ResponseEntity.ok(ApiResponse.ok("Treatment status updated",
                service.updateTreatmentStatus(id, status)));
    }

    @GetMapping("/treatments/assigned-by/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<TreatmentSummaryResponse>>> getByAssignedDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.ok("Treatments list for doctor " + doctorId,
                service.getTreatmentsByDoctorId(doctorId)));
    }
}
