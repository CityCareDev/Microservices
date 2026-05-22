package org.citycare.facilityservice.servicesImpl;

import lombok.extern.slf4j.Slf4j;
import org.citycare.facilityservice.dto.request.FacilityRequest;
import org.citycare.facilityservice.dto.response.FacilityResponse;
import org.citycare.facilityservice.dto.response.StaffResponse;
import org.citycare.facilityservice.entities.Facility;
import org.citycare.facilityservice.entities.Staff;
import org.citycare.facilityservice.exceptions.ResourceNotFoundException;
import org.citycare.facilityservice.repositories.FacilityRepository;
import org.citycare.facilityservice.repositories.StaffRepository;
import org.citycare.facilityservice.services.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional
    public FacilityResponse createFacility(FacilityRequest req) {
        Facility facility = Facility.builder()
                .name(req.getName())
                .type(req.getType())
                .location(req.getLocation())
                .capacity(req.getCapacity())
                .status(req.getStatus() != null ? req.getStatus() : Facility.Status.ACTIVE)
                .build();
        FacilityResponse saved = mapToResponse(facilityRepository.save(facility));
        return saved;
    }

    @Override
    @Transactional
    public FacilityResponse updateFacility(Long id, FacilityRequest req) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", id));

        facility.setName(req.getName());
        facility.setType(req.getType());
        facility.setLocation(req.getLocation());
        facility.setCapacity(req.getCapacity());
        if(req.getStatus() != null) facility.setStatus(req.getStatus());

        return mapToResponse(facilityRepository.save(facility));
    }

    @Override
    public FacilityResponse getById(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", id));
        return mapToResponse(facility);
    }

    @Override
    public Page<FacilityResponse> getAll(Pageable pageable) {
        return facilityRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public List<FacilityResponse> getByStatus(Facility.Status status) {
        // CHANGE: Map the list
        return facilityRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FacilityResponse> getByType(Facility.Type type) {
        // CHANGE: Map the list
        return facilityRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffByFacility(Long facilityId) {

        // 1. Ensure facility exists first
        if (!facilityRepository.existsById(facilityId)) {
            throw new ResourceNotFoundException("Facility", facilityId);
        }
        // 2. Fetch from repo and map to DTO list
        return staffRepository.findByFacilityFacilityId(facilityId).stream()
                .map(this::mapToStaffResponse) // Using the new helper method
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FacilityResponse updateStatus(Long id, Facility.Status status) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facility", id));
        facility.setStatus(status);
        return mapToResponse(facilityRepository.save(facility));
    }

//    this method is to map the ResponseDto to Entity

    private FacilityResponse mapToResponse(Facility facility) {
        return FacilityResponse.builder()
                .facilityId(facility.getFacilityId())
                .name(facility.getName())
                .type(facility.getType())
                .location(facility.getLocation())
                .capacity(facility.getCapacity())
                .status(facility.getStatus())
                .build();
    }

//    private StaffResponse mapToStaffResponse(Staff staff) {
//        return StaffResponse.builder()
//                .staffId(staff.getStaffId())
//                .name(staff.getName())
//                .role(staff.getRole())
//                .contactInfo(staff.getContactInfo())
//                .status(staff.getStatus())
//                .facilityId(staff.getFacility().getFacilityId()) // Get ID from the linked Facility object
    ////                .userId(staff.getUserId()) // The ID linking to the IAM Service
//                .build();
//    }
    private StaffResponse mapToStaffResponse(Staff staff) {
        return StaffResponse.builder()
                .staffId(staff.getStaffId())
                .userId(staff.getStaffId())  // userId and staffId are now the same value
                .name(staff.getName())
                .role(staff.getRole())
                .contactInfo(staff.getContactInfo())
                .status(staff.getStatus())
                .facilityId(staff.getFacility().getFacilityId())
                .build();
    }


}