package org.citycare.facilityservice.client;

import org.citycare.facilityservice.dto.request.StaffRequest;
import org.citycare.facilityservice.dto.response.ApiResponse;
import org.citycare.facilityservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "AUTHSERVICE")
public interface IamClient {

    // 1. Doctors

    @PostMapping("/admin/staff")
    ApiResponse<UserResponse> createStaffAccount(@RequestBody StaffRequest request);

    // 2. Dispatchers
    @PostMapping("/admin/dispatchers")
    ApiResponse<UserResponse> createDispatcherAccount(@RequestBody StaffRequest request);

    // 3. Compliance Officers
    @PostMapping("/admin/compliance-officers")
    ApiResponse<UserResponse> createComplianceAccount(@RequestBody StaffRequest request);

    // 4. Compensation for partial failures
    @PatchMapping("/admin/users/{id}/deactivate")
    ApiResponse<UserResponse> deactivateUser(@PathVariable("id") Long userId);

    @DeleteMapping("/admin/users/{id}")
    ApiResponse<Void> deleteUser(@PathVariable("id") Long userId);

    @GetMapping("/internal/users/by-email")
    ApiResponse<UserResponse> getUserByEmail(@RequestParam("email") String email);
}