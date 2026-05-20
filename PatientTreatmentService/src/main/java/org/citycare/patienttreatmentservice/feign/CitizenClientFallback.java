package org.citycare.patienttreatmentservice.feign;

import org.citycare.patienttreatmentservice.feign.dto.CitizenResponse;
import org.springframework.stereotype.Component;

@Component
public class CitizenClientFallback implements CitizenClient {

    @Override
    public CitizenResponse getById(Long citizenId) {
        // Fallback: return a minimal response so the system remains operational
        CitizenResponse fallback = new CitizenResponse();
        fallback.setCitizenId(citizenId);
        fallback.setName("Unknown (citizen-service unavailable)");
        fallback.setStatus("UNKNOWN");
        return fallback;
    }
}
