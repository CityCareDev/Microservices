package org.citycare.facilityservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.citycare.facilityservice.entities.Facility;

//we use dto classes, it will tell us, which data we want to show to the user or which  to hide
@Data
public class FacilityRequest {

    @NotBlank(message = "Facility name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Facility type is required (e.g., HOSPITAL, PARK, SCHOOL)")
    private Facility.Type type;

    @NotBlank(message = "Location address is required")
    private String location;

    @Min(value = 0, message = "Capacity cannot be negative")
    private int capacity;

    @NotNull(message = "Initial status is required")
    private Facility.Status status;
}
