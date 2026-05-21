package org.citycare.citizenservice.feign;

import org.citycare.citizenservice.dto.request.UserProfileUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "AUTHSERVICE")
public interface AuthClient {

    @PutMapping("/internal/users/{id}/profile")
    void updateUserProfile(
            @PathVariable Long id,
            @RequestBody UserProfileUpdateRequest request);

    @GetMapping("/internal/users/by-role")
    List<AdminUserResponse> getUsersByRole(@RequestParam("role") String role);

    record AdminUserResponse(Long id, String name, String email, String role) {}
}