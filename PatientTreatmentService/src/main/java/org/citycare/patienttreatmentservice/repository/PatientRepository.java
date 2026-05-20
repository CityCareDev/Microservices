package org.citycare.patienttreatmentservice.repository;


import org.citycare.patienttreatmentservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmergencyId(Long emergencyId);
    List<Patient> findByCitizenId(Long citizenId);
    List<Patient> findByStatus(Patient.Status status);
    List<Patient> findByFacilityIdAndStatus(Long facilityId, Patient.Status status);
    List<Patient> findByFacilityId(Long facilityId);
    List<Patient> findByAssignedStaffId(Long assignedStaffId);
    List<Patient> findByFacilityIdAndAssignedStaffIdIsNull(Long facilityId);
    List<Patient> findByAssignedStaffIdIsNull();
    List<Patient> findByFacilityIdAndAssignedStaffId(Long facilityId, Long assignedStaffId);
}