package org.citycare.facilityservice.services;

import org.citycare.facilityservice.dto.request.StaffRequest;
import org.citycare.facilityservice.dto.response.StaffResponse;
import org.citycare.facilityservice.entities.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {
    // Standard CRUD
    StaffResponse createStaff(StaffRequest request);
    StaffResponse getStaffById(Long id);
    Page<StaffResponse> getAllStaff(Pageable pageable);
    StaffResponse updateStaffStatus(Long id, Staff.Status status);

    // Business Logic
    List<StaffResponse> getStaffByFacility(Long facilityId);
    List<StaffResponse> getStaffByRole(Staff.Role role);
    void deleteStaff(Long id);
}
