package org.citycare.patienttreatmentservice.feign.dto;

import org.citycare.patienttreatmentservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FACILITYSERVICE")
public interface StaffClient {


    @GetMapping("/staff/{id}")
    ApiResponse<StaffResponse> getStaffById(@PathVariable("id") Long id);

}
