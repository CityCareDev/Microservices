package org.citycare.complianceservice.feign;
import org.citycare.complianceservice.feign.dto.FacilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FACILITYSERVICE", fallback = FacilityClientFallback.class)
public interface FacilityClient {

    @GetMapping("/facilities/{id}")
    FacilityResponse getFacilityById(@PathVariable("id") Long facilityId);
}
