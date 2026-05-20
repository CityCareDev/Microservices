package org.citycare.emergencyservice.feign;

import org.citycare.emergencyservice.feign.dto.CitizenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CITIZENSERVICE", fallback = CitizenClientFallback.class)
public interface CitizenClient {

    @GetMapping("/api/citizens/internal/{id}")
    CitizenResponse getById(@PathVariable("id") Long citizenId);

    @GetMapping("/api/citizens/internal/{id}/verified")
    boolean isCitizenVerified(@PathVariable("id") Long citizenId);

    @GetMapping("/api/citizens/internal/user/{userId}/citizen")
    CitizenResponse getCitizenByUserId(@PathVariable("userId") Long userId);
}
