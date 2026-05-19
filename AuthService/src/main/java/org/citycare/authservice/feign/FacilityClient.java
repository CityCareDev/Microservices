package org.citycare.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "FACILITYSERVICE")
public interface FacilityClient {

    @PatchMapping("/staff/{id}/status")
    void updateStaffStatus(@PathVariable("id") Long staffId, @RequestParam("status") String status);
}
