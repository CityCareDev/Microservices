package org.citycare.patienttreatmentservice.feign;


import org.citycare.patienttreatmentservice.feign.dto.EmergencyResponse;
import org.springframework.stereotype.Component;

@Component
public class EmergencyClientFallback implements EmergencyClient {

    @Override
    public EmergencyResponse getById(Long emergencyId) {
        EmergencyResponse fallback = new EmergencyResponse();
        fallback.setEmergencyId(emergencyId);
        fallback.setStatus("UNKNOWN");
        return fallback;
    }

    @Override
    public EmergencyResponse updateEmergencyStatus(Long emergencyId, String status) {
        EmergencyResponse fallback = new EmergencyResponse();
        fallback.setEmergencyId(emergencyId);
        fallback.setStatus(status);
        return fallback;
    }

    @Override
    public void releaseAmbulance(Long id) {

    }
}
