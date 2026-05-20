package org.citycare.emergencyservice.feign.dto;

import lombok.Data;

@Data
public class StaffResponse {
    private Long staffId;
    private Long userId;
    private String name;
    private String role;
    private String contactInfo;
    private String status;
    private Long facilityId;
}
