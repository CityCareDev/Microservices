package org.citycare.facilityservice.services;


import org.citycare.facilityservice.dto.request.FacilityRequest;
import org.citycare.facilityservice.dto.response.FacilityResponse;
import org.citycare.facilityservice.dto.response.StaffResponse;
import org.citycare.facilityservice.entities.Facility;
import org.citycare.facilityservice.entities.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FacilityService {
    FacilityResponse createFacility(FacilityRequest req);
    FacilityResponse updateFacility(Long id, FacilityRequest req);
    FacilityResponse getById(Long id);
    Page<FacilityResponse> getAll(Pageable pageable);
    List<FacilityResponse> getByStatus(Facility.Status status);
    List<FacilityResponse> getByType(Facility.Type type);
    List<StaffResponse> getStaffByFacility(Long facilityId);
    FacilityResponse updateStatus(Long id, Facility.Status status);
}
