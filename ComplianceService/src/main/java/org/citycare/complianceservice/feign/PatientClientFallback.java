package org.citycare.complianceservice.feign;

import org.citycare.complianceservice.feign.dto.PatientResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PatientClientFallback implements PatientClient {

    @Override
    public PatientResponse getPatientById(Long patientId) {
        log.warn("patient-treatment-service unavailable – cannot validate patientId={}", patientId);
        PatientResponse fallback = new PatientResponse();
        fallback.setPatientId(patientId);
        fallback.setStatus("UNKNOWN");
        return fallback;
    }
}
