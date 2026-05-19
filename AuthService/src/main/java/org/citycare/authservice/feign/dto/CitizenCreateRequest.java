package org.citycare.authservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenCreateRequest {
    private Long userId;
    private String name;
    private String contactInfo; // phone number
}
