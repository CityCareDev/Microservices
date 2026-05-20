package org.citycare.patienttreatmentservice.feign.dto;

import lombok.Data;

@Data
public class CitizenResponse {
    private Long citizenId;
    private String name;
    private String gender;
    private String address;
    private String contactInfo;
    private Long userId;
    private String status;
}
