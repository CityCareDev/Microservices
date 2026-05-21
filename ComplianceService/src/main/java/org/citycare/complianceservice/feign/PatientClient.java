package org.citycare.complianceservice.feign;

import org.citycare.complianceservice.feign.dto.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PATIENTTREATMENTSERVICE", fallback = PatientClientFallback.class)
public interface PatientClient {

    @GetMapping("/patients/{id}")
    PatientResponse getPatientById(@PathVariable("id") Long patientId);
}
