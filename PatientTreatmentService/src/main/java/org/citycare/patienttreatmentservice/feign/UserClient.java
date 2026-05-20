package org.citycare.patienttreatmentservice.feign;

import org.citycare.patienttreatmentservice.dto.ApiResponse;
import org.citycare.patienttreatmentservice.feign.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTHSERVICE", fallback = UserClientFallback.class) // Fallback unte add cheyyi
public interface UserClient {

    @GetMapping("/admin/users/{id}")
    ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") Long id);
}
