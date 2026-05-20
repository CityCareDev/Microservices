package org.citycare.patienttreatmentservice.repository;

import org.citycare.patienttreatmentservice.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByPatientPatientId(Long patientId);
    List<Treatment> findByAssignedById(Long assignedById);
    List<Treatment> findByPatientFacilityId(Long facilityId);
    List<Treatment> findByAssignedByIdAndPatientFacilityId(Long assignedById, Long facilityId);
}
