package org.citycare.complianceservice.feign;

import org.citycare.complianceservice.feign.dto.FacilityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FacilityClientFallback implements FacilityClient {

    @Override
    public FacilityResponse getFacilityById(Long facilityId) {
        log.warn("facility-service unavailable – cannot validate facilityId={}", facilityId);
        FacilityResponse fallback = new FacilityResponse();
        fallback.setFacilityId(facilityId);
        fallback.setName("Unknown (facility-service unavailable)");
        fallback.setStatus("UNKNOWN");
        return fallback;
    }
}
