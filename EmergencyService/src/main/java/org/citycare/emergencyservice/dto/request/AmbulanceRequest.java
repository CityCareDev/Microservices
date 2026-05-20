package org.citycare.emergencyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AmbulanceRequest {
    @NotBlank(message = "Vehicle number is required")
    @Pattern(regexp = "^[A-Z]{2,4}-\\d{2,4}$", message = "Vehicle number must follow pattern like AMB-001, KA-1234")
    @Size(min = 4, max = 10, message = "Vehicle number must be 4-10 characters")
    private String vehicleNumber;

    @Size(max = 50, message = "Model name must not exceed 50 characters")
    private String model;

    @NotNull(message = "Facility ID is required")
    private Long facilityId;
}
