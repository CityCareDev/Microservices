package org.citycare.patienttreatmentservice.feign;


import org.citycare.patienttreatmentservice.feign.dto.EmergencyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "EMERGENCYSERVICE", fallback = EmergencyClientFallback.class)
public interface EmergencyClient {

    @GetMapping("/emergencies/internal/{id}")
    EmergencyResponse getById(@PathVariable("id") Long emergencyId);

    @PutMapping("/emergencies/{id}/status")
    EmergencyResponse updateEmergencyStatus(@PathVariable("id") Long emergencyId,
                                            @RequestParam("status") String status);

    // EmergencyClient.java lo
    @PutMapping("/emergencies/{id}/release-ambulance") // Controller lo unna path correct ga ikkada ivvali
    void releaseAmbulance(@PathVariable("id") Long id);

}
