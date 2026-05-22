package org.citycare.emergencyservice.services;

import org.citycare.emergencyservice.dto.request.AmbulanceRequest;
import org.citycare.emergencyservice.dto.request.DispatchRequest;
import org.citycare.emergencyservice.dto.request.EmergencyRequest;
import org.citycare.emergencyservice.dto.response.EmergencyResponse;
import org.citycare.emergencyservice.entity.Ambulance;
import org.citycare.emergencyservice.entity.Emergency;
import org.citycare.emergencyservice.feign.dto.CitizenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmergencyService {
    // Emergency Reporting & Tracking
    Emergency reportEmergency(EmergencyRequest req);
    Emergency getById(Long id);
    Page<Emergency> getAllEmergencies(Pageable pageable);
    List<Emergency> getMyCases(Long citizenId);
    List<Emergency> getReportedEmergencies();
    List<Emergency> getDispatchedEmergencies();
    List<Emergency> getMyDispatchHistory(Long dispatcherId);
    Emergency updateEmergencyStatus(Long emergencyId, String status);
//    CitizenResponse getCitizenForEmergency(Long emergencyId);

    // Dispatch Operations
    Emergency dispatchAmbulance(Long emergencyId, Long dispatcherId, DispatchRequest req);

    // Ambulance Management
    Ambulance addAmbulance(AmbulanceRequest req);
    List<Ambulance> getAllAmbulances();
    List<Ambulance> getAvailableAmbulances();
    Ambulance updateAmbulanceStatus(Long id, Ambulance.Status status);
    void deleteAmbulance(Long id);


    //treatement
    EmergencyResponse getEmergencyResponseById(Long id);


    void releaseAmbulanceForEmergency(Long emergencyId);
}