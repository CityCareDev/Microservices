package org.citycare.emergencyservice.feign;

import org.citycare.emergencyservice.dto.response.ApiResponse;
import org.citycare.emergencyservice.feign.dto.StaffResponse;
import org.springframework.stereotype.Component;

@Component
public class StaffClientFallback implements StaffClient {

    @Override
    public ApiResponse<StaffResponse> getStaffById(Long staffId) {
        return ApiResponse.error("Facility service unavailable");
    }
}
