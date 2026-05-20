package org.citycare.patienttreatmentservice.serviceImplementation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.patienttreatmentservice.dto.AdmitPatientRequest;
import org.citycare.patienttreatmentservice.dto.ApiResponse;
import org.citycare.patienttreatmentservice.dto.TreatmentRequest;
import org.citycare.patienttreatmentservice.entity.Patient;
import org.citycare.patienttreatmentservice.entity.Treatment;
import org.citycare.patienttreatmentservice.exception.BadRequestException;
import org.citycare.patienttreatmentservice.exception.PatientAlreadyAdmittedException;
import org.citycare.patienttreatmentservice.exception.ResourceNotFoundException;
import org.citycare.patienttreatmentservice.exception.UnauthorizedException;
import org.citycare.patienttreatmentservice.feign.CitizenClient;
import org.citycare.patienttreatmentservice.feign.EmergencyClient;
import org.citycare.patienttreatmentservice.feign.UserClient;
import org.citycare.patienttreatmentservice.feign.dto.*;
import org.citycare.patienttreatmentservice.repository.PatientRepository;
import org.citycare.patienttreatmentservice.repository.TreatmentRepository;
import org.citycare.patienttreatmentservice.service.PatientTreatmentInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientTreatmentService implements PatientTreatmentInterface {

    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;

    // OpenFeign clients for cross-service communication
    private final CitizenClient citizenClient;
    private final EmergencyClient emergencyClient;
    private final UserClient userClient;
    private final StaffClient staffClient;
    // --------------------------------------------Patient -----------------------------------------

    @Transactional
    public Patient admitPatient(AdmitPatientRequest req) {
        log.info("Starting admission process for Emergency ID: {}", req.getEmergencyId());

        // 1. DUPLICATE CHECK (Validation Step)
        // Idhi mundhe check cheyali, lekunda citizenClient/emergencyClient call avvadam waste.
        patientRepository.findByEmergencyId(req.getEmergencyId()).ifPresent(p -> {
            log.error("Duplicate admission attempt. Emergency ID {} already has Patient ID {}",
                    req.getEmergencyId(), p.getPatientId());

            // Meeru adigina custom message ikkada undi
            throw new PatientAlreadyAdmittedException(
                    "Can't admit patient because he is already in the hospital. Patient ID: " + p.getPatientId());
        });

        // 2. Validate Citizen
        try {
            CitizenResponse citizen = citizenClient.getById(req.getCitizenId());
            log.info("Validated citizen: {} (id={})", citizen.getName(), citizen.getCitizenId());
        } catch (Exception e) {
            log.warn("Could not validate citizen {} from citizen-service: {}", req.getCitizenId(), e.getMessage());
        }

        // 3. Update Emergency Status
        try {
            EmergencyResponse emergency = emergencyClient.getById(req.getEmergencyId());
            log.info("Validated emergency id={} with status={}", emergency.getEmergencyId(), emergency.getStatus());

            // Status update chese mundhu check cheyadam better
            emergencyClient.updateEmergencyStatus(req.getEmergencyId(), "ADMITTED");
            log.info("Updated emergency {} status to ADMITTED", req.getEmergencyId());
        } catch (Exception e) {
            log.error("Failed to update emergency status: {}", e.getMessage());
            throw new RuntimeException("Emergency status update failed."); // Re-throw so @Transactional rolls back
        }

        // 4. Create and Save Patient Record
        Patient patient = Patient.builder()
                .citizenId(req.getCitizenId())
                .emergencyId(req.getEmergencyId())
                .facilityId(req.getFacilityId())
                .admissionDate(LocalDate.now())
                .ward(req.getWard())
                .notes(req.getNotes())
                .status(Patient.Status.ADMITTED)
                .build();

        Patient savedPatient = patientRepository.save(patient);

        // 5. Release Ambulance
        try {
            emergencyClient.releaseAmbulance(req.getEmergencyId());
            log.info("Ambulance released for Emergency ID: {}", req.getEmergencyId());
        } catch (Exception e) {
            log.error("Ambulance release failed: {}", e.getMessage());
            // Dependency issues unte ikkada custom logic handle cheyali
        }

        log.info("Patient successfully admitted with ID: {}", savedPatient.getPatientId());
        return savedPatient;
    }

    private boolean isDoctor(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DOCTOR"::equals);
    }

    private Long getCurrentUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("Invalid user identity in token");
        }
    }

    private StaffResponse getStaffOrThrow(Long staffId) {
        try {
            ApiResponse<StaffResponse> response = staffClient.getStaffById(staffId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Staff", staffId);
            }
            return response.getData();
        } catch (feign.FeignException e) {
            log.error("Feign error while fetching staff {}: {}", staffId, e.status());
            if (e.status() == 404) {
                throw new ResourceNotFoundException("Staff", staffId);
            }
            if (e.status() == 403) {
                throw new UnauthorizedException("Access denied while validating staff.");
            }
            throw new RuntimeException("Facility Service error: " + e.status());
        } catch (Exception e) {
            log.error("Failed to fetch staff {}: {}", staffId, e.getMessage());
            throw new RuntimeException("Could not validate staff. Facility Service might be down.");
        }
    }

    private Long getCurrentDoctorFacilityId(Authentication auth) {
        Long userId = getCurrentUserId(auth);
        StaffResponse currentStaff = getStaffOrThrow(userId);
        if (currentStaff.getFacilityId() == null) {
            throw new BadRequestException("Doctor is not mapped to any facility.");
        }
        return currentStaff.getFacilityId();
    }

    private void ensureDoctorFacilityAccess(Authentication auth, Long targetFacilityId) {
        if (!isDoctor(auth)) return;
        Long doctorFacilityId = getCurrentDoctorFacilityId(auth);
        if (targetFacilityId == null || !doctorFacilityId.equals(targetFacilityId)) {
            throw new UnauthorizedException("Doctor can only access patients within assigned facility.");
        }
    }

    public List<Patient> getAllPatients() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            return patientRepository.findByFacilityId(getCurrentDoctorFacilityId(auth));
        }
        return patientRepository.findAll();
    }

    public List<Patient> getPatientsByFacility(Long facilityId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ensureDoctorFacilityAccess(auth, facilityId);
        return patientRepository.findByFacilityId(facilityId);
    }

    public Patient getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ensureDoctorFacilityAccess(auth, patient.getFacilityId());
        return patient;
    }

    @Transactional
    public Patient updatePatientStatus(Long id, Patient.Status status) {
        log.info("Updating status for Patient ID: {} to {}", id, status);

        // 1. Fetch Patient and Check Existence
        // getPatientById(id) lo already orElseThrow(ResourceNotFoundException) undi ani anukuntunna
        Patient patient = getPatientById(id);

        // 2. Business Logic: Discharged ayina patient ni malli update cheyanivvakudadu
        if (patient.getStatus() == Patient.Status.DISCHARGED) {
            throw new BadRequestException("Patient already discharged! Cannot update status anymore.");
        }

        // 3. Business Logic: Only STABLE patients can be discharged
        if (status == Patient.Status.DISCHARGED && patient.getStatus() != Patient.Status.STABLE) {
            throw new BadRequestException("Cannot discharge patient. Patient status must be STABLE before discharge. Current status: " + patient.getStatus());
        }

        // 4. Update Local Patient Status
        patient.setStatus(status);
        if (status == Patient.Status.DISCHARGED) {
            patient.setDischargeDate(LocalDate.now());
        }

        // 4. External Service Call (Emergency Service)
        if (status == Patient.Status.DISCHARGED) {
            try {
                log.info("Notifying Emergency Service to CLOSE Emergency ID: {}", patient.getEmergencyId());
                emergencyClient.updateEmergencyStatus(patient.getEmergencyId(), "CLOSED");
                log.info("Emergency {} successfully CLOSED via Feign", patient.getEmergencyId());
            } catch (Exception e) {
                // Ikkada log error chestunnam kani process aapatledu (Soft Fail)
                // Endukante Patient discharge avvadam main priority
                log.error("Failed to close emergency {} in emergency-service: {}",
                        patient.getEmergencyId(), e.getMessage());

                // Optional: System critical ayithe ikkada kuda throw RuntimeException kottochu
                // throw new RuntimeException("Could not sync with Emergency Service");
            }
        }

        // 5. Save Changes to Local DB
        try {
            Patient updatedPatient = patientRepository.save(patient);
            log.info("Patient ID: {} status updated to {} successfully", id, status);

            return updatedPatient;
        } catch (Exception e) {
            log.error("Database error while updating patient status: {}", e.getMessage());
            throw new RuntimeException("Failed to save patient status update");
        }
    }

    public EmergencyResponse getEmergencyForPatient(Long patientId) {
        Patient patient = getPatientById(patientId);
        return emergencyClient.getById(patient.getEmergencyId());
    }

    public List<Patient> getUnassignedPatients() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            return patientRepository.findByFacilityIdAndAssignedStaffIdIsNull(getCurrentDoctorFacilityId(auth));
        }
        return patientRepository.findByAssignedStaffIdIsNull();
    }

    public List<Patient> getUnassignedPatientsByFacility(Long facilityId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ensureDoctorFacilityAccess(auth, facilityId);
        return patientRepository.findByFacilityIdAndAssignedStaffIdIsNull(facilityId);
    }

    public List<Patient> getPatientsByDoctor(Long doctorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            // New rule: doctors can view all patients in their facility, not only own assignments.
            return patientRepository.findByFacilityId(getCurrentDoctorFacilityId(auth));
        }
        return patientRepository.findByAssignedStaffId(doctorId);
    }

    public List<Patient> getPatientsByFacilityAndDoctor(Long facilityId, Long doctorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            ensureDoctorFacilityAccess(auth, facilityId);
            // New rule: doctors can view all patients in their facility, even if assigned to another doctor.
            return patientRepository.findByFacilityId(facilityId);
        }
        return patientRepository.findByFacilityIdAndAssignedStaffId(facilityId, doctorId);
    }

    // --------------------------------------------  Treatment -------------------------------------------------------

    public List<Patient> getPatientsByStatus(Patient.Status status) {
        log.info("Fetching patients with status: {}", status);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            return patientRepository.findByFacilityIdAndStatus(getCurrentDoctorFacilityId(auth), status);
        }
        return patientRepository.findByStatus(status);
    }

    @Transactional
    public Treatment addTreatment( TreatmentRequest req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        Long assignedById = getCurrentUserId(auth);
        log.info("Attempting to add treatment for Patient ID: {} by Staff ID: {}", req.getPatientId(), assignedById);

        // 1. Fetch Patient and Validate Status
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", req.getPatientId()));

        if (patient.getStatus() == Patient.Status.DISCHARGED) {
            log.warn("Treatment assignment failed: Patient {} is already discharged", req.getPatientId());
            throw new BadRequestException("Cannot assign treatment. Patient is already DISCHARGED.");
        }

        // 2. Fetch Staff via Feign Client with Precise Error Handling
        StaffResponse staff = getStaffOrThrow(assignedById);
        log.info("Validated Staff: {} | Role: {}", staff.getName(), staff.getRole());

        // 3. Staff Role Validation
        String role = staff.getRole();
        if (role == null || !role.equalsIgnoreCase("DOCTOR")) {
            log.warn("Unauthorized attempt by ID: {} with role: {}", assignedById, role);
            throw new BadRequestException("Unauthorized: Only DOCTOR can assign treatments. Current role: " + role);
        }

        if (patient.getFacilityId() == null || staff.getFacilityId() == null
                || !patient.getFacilityId().equals(staff.getFacilityId())) {
            throw new UnauthorizedException("Doctor can only add treatment for patients in the same assigned facility.");
        }

        // 4. Create and Save Treatment
        // Assign the patient to this staff if not already assigned.
        // If already assigned to another doctor, only visibility is allowed, not treatment creation.
        if (patient.getAssignedStaffId() == null) {
            patient.setAssignedStaffId(assignedById);
            patientRepository.save(patient);
            log.info("Patient {} assigned to Staff ID: {}", patient.getPatientId(), assignedById);
        } else if (!patient.getAssignedStaffId().equals(assignedById)) {
            throw new UnauthorizedException(
                    "Patient is assigned to another doctor. You can view patient details but cannot add treatment.");
        }

        Treatment treatment = Treatment.builder()
                .patient(patient)
                .assignedById(assignedById)
                .description(req.getDescription())
                .medicationName(req.getMedicationName())
                .dosage(req.getDosage())
                .startDate(LocalDate.now())
                .status(Treatment.Status.ONGOING)
                .build();

        Treatment savedTreatment = treatmentRepository.save(treatment);
        log.info("Treatment (ID: {}) successfully assigned by {}", savedTreatment.getTreatmentId(), staff.getName());

        return savedTreatment;
    }
    public List<Treatment> getTreatmentsByPatient(Long patientId) {
        // Reuse existing secured patient fetch so doctor access is facility-scoped.
        getPatientById(patientId);
        return treatmentRepository.findByPatientPatientId(patientId);
    }

    public Treatment getTreatmentById(Long id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            Long doctorFacilityId = getCurrentDoctorFacilityId(auth);
            Long treatmentFacilityId = treatment.getPatient() != null ? treatment.getPatient().getFacilityId() : null;
            if (treatmentFacilityId == null || !doctorFacilityId.equals(treatmentFacilityId)) {
                throw new UnauthorizedException("Doctor can only access treatments within assigned facility.");
            }
        }
        return treatment;
    }
//    @Transactional
//    public Treatment updateTreatmentStatus(Long id, Treatment.Status status) {
//        log.info("Updating status for Treatment ID: {} to {}", id, status);
//
//        // 1. Fetch Treatment and Validate Existence
//        // findById(id) lo 'id' ni vethukuthunnam
//        Treatment treatment = treatmentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Treatment", id));
//
//        // 2. Business Logic: Status Updates
//        try {
//            // Kotha status ni set chestunnam
//            treatment.setStatus(status);
//
//            // Oka vela treatment COMPLETED leda CANCELLED ayithe, end date ivvala set cheyali
//            if (status == Treatment.Status.COMPLETED || status == Treatment.Status.CANCELLED) {
//                treatment.setEndDate(LocalDate.now());
//                log.info("Treatment ID: {} marked as {}, setting end date to today.", id, status);
//            }
//
//            // 3. Save to Database
//            Treatment updatedTreatment = treatmentRepository.save(treatment);
//            log.info("Treatment ID: {} status updated successfully", id);
//
//            return updatedTreatment;
//
//        } catch (Exception e) {
//            log.error("Error occurred while updating treatment status for ID {}: {}", id, e.getMessage());
//            // Database issue vaste generic RuntimeException throw chestunnam
//            throw new RuntimeException("Could not update treatment status due to a server error.");
//        }
//    }

    @Transactional
    public Treatment updateTreatmentStatus(Long id, Treatment.Status newStatus) {
        log.info("Updating status for Treatment ID: {} to {}", id, newStatus);

        // 1. Fetch Treatment and Validate Existence
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            Long currentDoctorId = getCurrentUserId(auth);
            Long doctorFacilityId = getCurrentDoctorFacilityId(auth);
            Long treatmentFacilityId = treatment.getPatient() != null ? treatment.getPatient().getFacilityId() : null;

            if (treatmentFacilityId == null || !doctorFacilityId.equals(treatmentFacilityId)) {
                throw new UnauthorizedException("Doctor can only update treatments within assigned facility.");
            }
            if (!currentDoctorId.equals(treatment.getAssignedById())) {
                throw new UnauthorizedException("Only the assigned doctor can update this treatment.");
            }
        }

        // 2. Business Logic: Finalized Status Check
        // Treatment already COMPLETED leda CANCELLED ayithe, inka marchanivvakudadu
        if (treatment.getStatus() == Treatment.Status.COMPLETED ||
                treatment.getStatus() == Treatment.Status.CANCELLED) {

            log.warn("Update failed: Treatment ID {} is already in final state: {}", id, treatment.getStatus());
            throw new BadRequestException("Treatment is already finalized (" + treatment.getStatus() + ") and cannot be updated.");
        }

        try {
            // 3. Update Status
            treatment.setStatus(newStatus);

            // 4. Handle End Date
            if (newStatus == Treatment.Status.COMPLETED || newStatus == Treatment.Status.CANCELLED) {
                treatment.setEndDate(LocalDate.now());
                log.info("Treatment ID: {} marked as {}, setting end date to today.", id, newStatus);
            }

            // 5. Save to Database
            Treatment updatedTreatment = treatmentRepository.save(treatment);
            log.info("Treatment ID: {} status updated successfully to {}", id, newStatus);

            return updatedTreatment;

        } catch (Exception e) {
            log.error("Error occurred while updating treatment status for ID {}: {}", id, e.getMessage());
            // Generic RuntimeException to ensure transaction rollback
            throw new RuntimeException("Could not update treatment status due to a server error.");
        }
    }
    @Transactional(readOnly = true)
    public List<Treatment> getAllTreatments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDoctor(auth)) {
            return treatmentRepository.findByPatientFacilityId(getCurrentDoctorFacilityId(auth));
        }
        return treatmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TreatmentSummaryResponse> getMyAssigned(Long staffId) {
        log.info("Fetching all treatments assigned by Staff ID: {}", staffId);

        // 1. Staff check (Feign Call badulu simple ga validate cheyali)
        // Note: Database query lo records rakapothe staff lenatte or treatments lenatte.
        // Oka vela strict ga staff existence check cheyalante userClient call cheyali.
        try {
            ResponseEntity<ApiResponse<UserResponse>> staffResponse = userClient.getUserById(staffId);
            ApiResponse<UserResponse> body = staffResponse.getBody();
            if (body == null || body.getData() == null) {
                throw new ResourceNotFoundException("Staff", staffId);
            }
        } catch (Exception e) {
            log.warn("Could not verify staff {} from AuthService, proceeding to fetch treatments anyway.", staffId);
        }

        try {
            // 2. Database nunchi treatments list tevali
            List<Treatment> treatments = treatmentRepository.findByAssignedById(staffId);

            if (CollectionUtils.isEmpty(treatments)) {
                log.info("No treatments found for staff ID: {}", staffId);
                return new ArrayList<>();
            }

            // 3. Result list create cheyali
            List<TreatmentSummaryResponse> responseList = new ArrayList<>();

            // 4. For-each loop logic
            for (Treatment t : treatments) {
                TreatmentSummaryResponse dto = TreatmentSummaryResponse.builder()
                        .treatmentId(t.getTreatmentId())
                        .patientId(t.getPatient().getPatientId())
                        .description(t.getDescription())
                        .medicationName(t.getMedicationName())
                        .dosage(t.getDosage())
                        .status(t.getStatus().toString())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .build();

                responseList.add(dto);
            }

            log.info("Successfully fetched {} treatments for staff ID: {}", responseList.size(), staffId);
            return responseList;

        } catch (Exception e) {
            log.error("Error in getMyAssigned for staff {}: {}", staffId, e.getMessage());
            throw new RuntimeException("Server error while fetching assigned treatments.");
        }
    }

    @Transactional(readOnly = true)
    public List<TreatmentSummaryResponse> getTreatmentsByDoctorId(Long doctorId) {
        log.info("Fetching treatments from DB for assignedById: {}", doctorId);

        // 1. Database nundi direct ga list techi
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Treatment> treatments;
        if (isDoctor(auth)) {
            Long doctorFacilityId = getCurrentDoctorFacilityId(auth);
            treatments = treatmentRepository.findByAssignedByIdAndPatientFacilityId(doctorId, doctorFacilityId);
        } else {
            treatments = treatmentRepository.findByAssignedById(doctorId);
        }

        if (CollectionUtils.isEmpty(treatments)) {
            log.warn("No treatments found for Doctor ID: {}", doctorId);
            return new ArrayList<>();
        }

        // 2. DTO loki mapping (Extra API calls lekunda)
        List<TreatmentSummaryResponse> responseList = new ArrayList<>();
        for (Treatment t : treatments) {
            responseList.add(TreatmentSummaryResponse.builder()
                    .treatmentId(t.getTreatmentId())
                    .patientId(t.getPatient().getPatientId())
                    .description(t.getDescription())
                    .medicationName(t.getMedicationName())
                    .dosage(t.getDosage())
                    .status(t.getStatus().toString())
                    .startDate(t.getStartDate())
                    .endDate(t.getEndDate())
                    // Doctor Name manam fetch cheyatledhu kabatti ID ne pampochu leda empty pettocchu

                    .build());
        }

        return responseList;
    }

    // ── Cross-service enrichment ──────────────────────────────────────────────

    /**
     * Fetches citizen details from citizen-service for a given patient.
     * Used to enrich patient data with citizen info via OpenFeign.
     */
//    public CitizenResponse getCitizenForPatient(Long patientId) {
//        Patient patient = getPatientById(patientId);
//        return citizenClient.getCitizenById(patient.getCitizenId());
//    }

    /**
     * Fetches emergency details from emergency-service for a given patient.
     */

}
