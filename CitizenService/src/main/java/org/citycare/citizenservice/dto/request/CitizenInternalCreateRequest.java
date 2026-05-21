package org.citycare.citizenservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CitizenInternalCreateRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String name;

    private String contactInfo; // phone from registration
}
