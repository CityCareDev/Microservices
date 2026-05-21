package org.citycare.complianceservice.feign;

import org.citycare.complianceservice.feign.dto.EmergencyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmergencyClientFallback implements EmergencyClient {

    @Override
    public EmergencyResponse getEmergencyById(Long emergencyId) {
        log.warn("emergency-service unavailable – cannot validate emergencyId={}", emergencyId);
        EmergencyResponse fallback = new EmergencyResponse();
        fallback.setEmergencyId(emergencyId);
        fallback.setStatus("UNKNOWN");
        return fallback;
    }
}
