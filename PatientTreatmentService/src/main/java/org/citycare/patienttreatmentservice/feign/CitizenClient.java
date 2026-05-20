package org.citycare.patienttreatmentservice.feign;


import org.citycare.patienttreatmentservice.feign.dto.CitizenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CITIZENSERVICE", fallback = CitizenClientFallback.class)
public interface CitizenClient {


    @GetMapping("/api/citizens/internal/{id}")
    CitizenResponse getById(@PathVariable("id") Long citizenId);
}
