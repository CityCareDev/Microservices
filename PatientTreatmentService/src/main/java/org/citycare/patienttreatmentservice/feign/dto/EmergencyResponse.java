package org.citycare.patienttreatmentservice.feign.dto;

import lombok.Data;

@Data
public class EmergencyResponse {
    private Long emergencyId;
    private Long citizenId;
    private String type;
    private String location;
    private String description;
    private String status;
    private Long dispatcherId;
}
