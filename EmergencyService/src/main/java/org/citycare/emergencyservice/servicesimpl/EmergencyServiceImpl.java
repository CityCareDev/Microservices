package org.citycare.emergencyservice.servicesimpl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.citycare.emergencyservice.dto.request.AmbulanceRequest;
import org.citycare.emergencyservice.dto.request.DispatchRequest;
import org.citycare.emergencyservice.dto.request.EmergencyRequest;
import org.citycare.emergencyservice.dto.response.EmergencyResponse;
import org.citycare.emergencyservice.entity.Ambulance;
import org.citycare.emergencyservice.entity.Emergency;
import org.citycare.emergencyservice.exception.BadRequestException;
import org.citycare.emergencyservice.exception.ResourceNotFoundException;
//import org.citycare.emergencyservice.feign.CitizenClient;
//import org.citycare.emergencyservice.feign.dto.CitizenResponse;
import org.citycare.emergencyservice.feign.CitizenClient;
import org.citycare.emergencyservice.feign.StaffClient;
import org.citycare.emergencyservice.feign.dto.CitizenResponse;
import org.citycare.emergencyservice.feign.dto.StaffResponse;
import org.citycare.emergencyservice.repository.AmbulanceRepository;
import org.citycare.emergencyservice.repository.EmergencyRepository;
import org.citycare.emergencyservice.services.EmergencyService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyServiceImpl implements EmergencyService{


    private final EmergencyRepository emergencyRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final CitizenClient citizenClient;
    private final StaffClient staffClient;



    @Override
    @Transactional
    public Emergency reportEmergency(EmergencyRequest req) {
        // 1. Security Context nundi User ID (Citizen ID) extract cheyadam
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated to report emergency");
        }

        // JwtFilter lo userId ni principal ga pettam kabatti ikkada direct ga parse cheyochu
        // In this system citizenId == userId (citizen profile PK is set to userId on registration)
        Long citizenId = Long.parseLong(auth.getName());
        log.info("Reporting emergency for Citizen ID: {}", citizenId);

        // 2. Validate Citizen exists
        CitizenResponse citizen;
        try {
            citizen = citizenClient.getById(citizenId);
            log.info("Validated citizen: {} (id={})", citizen.getName(), citizen.getCitizenId());
        } catch (Exception e) {
            log.warn("Citizen validation failed for ID {}: {}", citizenId, e.getMessage());
            throw new BadRequestException("Citizen profile not found. Cannot report emergency.");
        }

        // 3. Verify citizen has at least one verified document (server-side enforcement)
        try {
            boolean verified = citizenClient.isCitizenVerified(citizenId);
            if (!verified) {
                throw new BadRequestException("Document verification required before reporting emergencies. Please upload and get your documents verified.");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Document verification check failed for citizen {}: {}", citizenId, e.getMessage());
            throw new BadRequestException("Unable to verify document status. Please try again later.");
        }

        // 3. Create Emergency with the extracted citizenId
        Emergency emergency = Emergency.builder()
                .citizenId(citizenId)
                .type(req.getType())
                .location(req.getLocation())
                .description(req.getDescription())
                .status(Emergency.Status.REPORTED)
                .build();

        Emergency saved = emergencyRepository.save(emergency);
        return saved;
    }
    @Override
    @Transactional
    public Emergency dispatchAmbulance(Long emergencyId, Long dispatcherId, DispatchRequest req) {
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", emergencyId));

        if (emergency.getStatus() != Emergency.Status.REPORTED) {
            throw new BadRequestException("Cannot dispatch – status is " + emergency.getStatus());
        }

        Ambulance ambulance = ambulanceRepository.findById(req.getAmbulanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance", req.getAmbulanceId()));

        Long dispatcherFacilityId = resolveDispatcherFacilityId(dispatcherId);
        if (!dispatcherFacilityId.equals(ambulance.getFacilityId())) {
            throw new BadRequestException("Dispatcher can only dispatch ambulances from their assigned facility");
        }

        if (ambulance.getStatus() != Ambulance.Status.AVAILABLE) {
            throw new BadRequestException("Ambulance " + ambulance.getVehicleNumber() + " is not available");
        }

        ambulance.setStatus(Ambulance.Status.DISPATCHED);
        ambulanceRepository.save(ambulance);

        emergency.setStatus(Emergency.Status.DISPATCHED);
        emergency.setDispatcherId(dispatcherId);
        emergency.setAmbulance(ambulance);
        emergency.setDispatchedAt(LocalDateTime.now());
        Emergency saved = emergencyRepository.save(emergency);
        return saved;
    }

    @Override
    public List<Ambulance> getAvailableAmbulances() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDispatcher(auth)) {
            Long dispatcherId = parseUserId(auth);
            Long facilityId = resolveDispatcherFacilityId(dispatcherId);
            return ambulanceRepository.findByFacilityIdAndStatus(facilityId, Ambulance.Status.AVAILABLE);
        }
        return ambulanceRepository.findByStatus(Ambulance.Status.AVAILABLE);
    }

    @Override
    public List<Emergency> getReportedEmergencies() {
        return emergencyRepository.findByStatusOrderByCreatedAtDesc(Emergency.Status.REPORTED);
    }

    @Override
    public List<Emergency> getDispatchedEmergencies() {
        return emergencyRepository.findByStatus(Emergency.Status.DISPATCHED);
    }

    @Override
    public List<Emergency> getMyDispatchHistory(Long dispatcherId) {
        return emergencyRepository.findByDispatcherIdOrderByDispatchedAtDesc(dispatcherId);
    }

    @Override
    public Emergency getById(Long id) {
        return emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", id));
    }

    @Override
    public Page<Emergency> getAllEmergencies(Pageable pageable) {
        return emergencyRepository.findAll(pageable);
    }

    @Override
    public List<Emergency> getMyCases(Long citizenId) {
        return emergencyRepository.findByCitizenId(citizenId);
    }

    @Override
    @Transactional
    public Emergency updateEmergencyStatus(Long emergencyId, String statusStr) {
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", emergencyId));
        Emergency.Status status = Emergency.Status.valueOf(statusStr.toUpperCase());
        emergency.setStatus(status);
        log.info("Emergency {} status updated to {} (called via OpenFeign)", emergencyId, status);
        return emergencyRepository.save(emergency);
    }

//    @Override
//    public CitizenResponse getCitizenForEmergency(Long emergencyId) {
//        Emergency emergency = getById(emergencyId);
//        return citizenClient.getCitizenById(emergency.getCitizenId());
//    }

    @Override
    @Transactional
    public Ambulance addAmbulance(AmbulanceRequest req) {
        return ambulanceRepository.save(Ambulance.builder()
                .vehicleNumber(req.getVehicleNumber())
                .model(req.getModel())
                .facilityId(req.getFacilityId())
                .status(Ambulance.Status.AVAILABLE)
                .build());
    }

    @Override
    public List<Ambulance> getAllAmbulances() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isDispatcher(auth)) {
            Long dispatcherId = parseUserId(auth);
            Long facilityId = resolveDispatcherFacilityId(dispatcherId);
            return ambulanceRepository.findByFacilityId(facilityId);
        }
        return ambulanceRepository.findAll();
    }

    @Override
    @Transactional
    public Ambulance updateAmbulanceStatus(Long id, Ambulance.Status status) {
        Ambulance amb = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance", id));
        amb.setStatus(status);
        return ambulanceRepository.save(amb);
    }

    @Override
    @Transactional
    public void deleteAmbulance(Long id) {
        Ambulance amb = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance", id));
        if (amb.getStatus() == Ambulance.Status.DISPATCHED) {
            throw new IllegalStateException("Cannot delete an ambulance that is currently dispatched");
        }
        ambulanceRepository.delete(amb);
    }



    @Override
    public EmergencyResponse getEmergencyResponseById(Long id) {
        Emergency e = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", id));

        return EmergencyResponse.builder()
                .emergencyId(e.getEmergencyId())
                .citizenId(e.getCitizenId())
                .status(e.getStatus().name())
                .ambulanceId(
                        e.getAmbulance() != null ? e.getAmbulance().getAmbulanceId() : null
                )
                .build();
    }


    @Transactional
    public void releaseAmbulanceForEmergency(Long emergencyId) {
        // 1. Fetch the emergency
        Emergency emergency = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", emergencyId));

        // 2. Get the associated ambulance entity
        Ambulance ambulance = emergency.getAmbulance();

        if (ambulance == null) {
            throw new BadRequestException("No ambulance was dispatched for this emergency.");
        }

        // 3. Update the ambulance status
        ambulance.setStatus(Ambulance.Status.AVAILABLE);

        // 4. Save the updated ambulance
        ambulanceRepository.save(ambulance);
        log.info("Ambulance {} released and marked AVAILABLE for Emergency {}", ambulance.getVehicleNumber(), emergencyId);
    }

    private boolean isDispatcher(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DISPATCHER"::equals);
    }

    private Long parseUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new BadRequestException("Authenticated user not found");
        }
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid authenticated user id");
        }
    }

    private Long resolveDispatcherFacilityId(Long dispatcherId) {
        try {
            var response = staffClient.getStaffById(dispatcherId);
            StaffResponse staff = response != null ? response.getData() : null;
            if (staff == null || staff.getFacilityId() == null) {
                throw new BadRequestException("Dispatcher is not assigned to any facility");
            }
            return staff.getFacilityId();
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to resolve facility for dispatcher {}: {}", dispatcherId, ex.getMessage());
            throw new BadRequestException("Unable to determine dispatcher facility");
        }
    }

}
