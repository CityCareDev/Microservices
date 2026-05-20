package org.citycare.patienttreatmentservice.feign;

import org.citycare.patienttreatmentservice.dto.ApiResponse;
import org.citycare.patienttreatmentservice.feign.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component // Idi marchipoku, appude Spring deenni bean ga gurtistundi
public class UserClientFallback implements UserClient {

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(Long id) {
        // Fallback logic: Service down ayinappudu empty or error message thoti response pampali
        UserResponse fallbackUser = new UserResponse();
        fallbackUser.setUserId(id);
        fallbackUser.setName("Service Unavailable");
        fallbackUser.setRole("UNKNOWN");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(
                false,
                "AuthService is currently down. Please try again later.",
                fallbackUser
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}