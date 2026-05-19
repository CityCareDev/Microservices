package org.citycare.authservice.feign;

import org.citycare.authservice.feign.dto.CitizenCreateRequest;
import org.citycare.authservice.feign.dto.CitizenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CITIZENSERVICE", fallback = CitizenClientFallback.class)
public interface CitizenClient {

    /**
     * Called after a new CITIZEN user registers — auto-creates a citizen profile
     * in citizen-service via OpenFeign.
     */
    @PostMapping("/api/citizens/internal/create")
    CitizenResponse createCitizenProfile(@RequestBody CitizenCreateRequest request);
}
