package org.citycare.patienttreatmentservice.feign.dto;

import lombok.Data;
// Nee service package

import lombok.Data;

@Data
public class StaffResponse {
    private Long staffId;
    private String name;
    private String role;       // Role enum kakunda String ga teeskodam safe mapping ki
    private String contactInfo;
    private String status;
    private Long facilityId;
}