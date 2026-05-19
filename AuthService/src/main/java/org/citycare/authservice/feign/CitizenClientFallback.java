package org.citycare.authservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.citycare.authservice.feign.dto.CitizenCreateRequest;
import org.citycare.authservice.feign.dto.CitizenResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CitizenClientFallback implements CitizenClient {

    @Override
    public CitizenResponse createCitizenProfile(CitizenCreateRequest request) {
        log.warn("citizen-service unavailable – citizen profile for userId={} will not be auto-created. " +
                 "Citizen can create their profile later via /api/citizens/profile", request.getUserId());
        return null;
    }
}
