package org.citycare.emergencyservice.feign;

import org.citycare.emergencyservice.feign.dto.CitizenResponse;
import org.springframework.stereotype.Component;

@Component
public class CitizenClientFallback implements CitizenClient {

    @Override
    public CitizenResponse getById(Long citizenId) {
        CitizenResponse fallback = new CitizenResponse();
        fallback.setCitizenId(citizenId);
        fallback.setName("Unknown (citizen-service unavailable)");
        fallback.setStatus("UNKNOWN");
        return fallback;
    }

    @Override
    public CitizenResponse getCitizenByUserId(Long userId) {
        CitizenResponse fallback = new CitizenResponse();
        fallback.setUserId(userId);
        fallback.setName("Unknown (citizen-service unavailable)");
        fallback.setStatus("UNKNOWN");
        return fallback;
    }

    @Override
    public boolean isCitizenVerified(Long citizenId) {
        return false; // fail-safe: deny if citizen-service is unavailable
    }
}
