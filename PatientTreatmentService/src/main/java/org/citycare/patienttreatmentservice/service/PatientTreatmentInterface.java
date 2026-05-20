package org.citycare.patienttreatmentservice.service;

import org.citycare.patienttreatmentservice.dto.AdmitPatientRequest;
import org.citycare.patienttreatmentservice.dto.TreatmentRequest;
import org.citycare.patienttreatmentservice.entity.Patient;
import org.citycare.patienttreatmentservice.entity.Treatment;
import org.citycare.patienttreatmentservice.feign.dto.CitizenResponse;
import org.citycare.patienttreatmentservice.feign.dto.EmergencyResponse;
import org.citycare.patienttreatmentservice.feign.dto.TreatmentSummaryResponse;

import java.util.List;

public interface PatientTreatmentInterface {
    //    Patient admitPatient(AdmitPatientRequest req);
    List<Patient> getAllPatients();
    Patient updatePatientStatus(Long id, Patient.Status status);
    Treatment addTreatment(TreatmentRequest req);
    List<Treatment> getTreatmentsByPatient(Long patientId);
    Treatment getTreatmentById(Long id);
    Treatment updateTreatmentStatus(Long id, Treatment.Status status);
    List<Treatment> getAllTreatments();
    //    CitizenResponse getCitizenForPatient(Long patientId);
    EmergencyResponse getEmergencyForPatient(Long patientId);
    List<TreatmentSummaryResponse> getTreatmentsByDoctorId(Long doctorId);
    List<Patient> getUnassignedPatients();
    List<Patient> getUnassignedPatientsByFacility(Long facilityId);
    List<Patient> getPatientsByDoctor(Long doctorId);
    List<Patient> getPatientsByFacilityAndDoctor(Long facilityId, Long doctorId);
}
