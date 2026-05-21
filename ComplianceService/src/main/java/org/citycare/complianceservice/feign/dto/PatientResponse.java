package org.citycare.complianceservice.feign.dto;

import lombok.Data;

@Data
public class PatientResponse {
    private Long patientId;
    private Long citizenId;
    private Long emergencyId;
    private String ward;
    private String status;
}
