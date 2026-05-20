package org.citycare.emergencyservice.feign;

import org.citycare.emergencyservice.dto.response.ApiResponse;
import org.citycare.emergencyservice.feign.dto.StaffResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FACILITYSERVICE", fallback = StaffClientFallback.class)
public interface StaffClient {

    @GetMapping("/staff/{id}")
    ApiResponse<StaffResponse> getStaffById(@PathVariable("id") Long staffId);
}
