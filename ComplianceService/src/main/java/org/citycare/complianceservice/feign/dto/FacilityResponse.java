package org.citycare.complianceservice.feign.dto;

import lombok.Data;

@Data
public class FacilityResponse {
    private Long facilityId;
    private String name;
    private String type;
    private String location;
    private int capacity;
    private String status;
}
