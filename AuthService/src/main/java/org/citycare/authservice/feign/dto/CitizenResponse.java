package org.citycare.authservice.feign.dto;

import lombok.Data;

@Data
public class CitizenResponse {
    private Long citizenId;
    private String name;
    private String contactInfo;
    private Long userId;
    private String status;
}
