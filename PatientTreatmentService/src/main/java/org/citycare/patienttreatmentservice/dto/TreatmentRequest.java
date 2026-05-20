package org.citycare.patienttreatmentservice.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TreatmentRequest {
    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be a valid positive number")
    @Min(value = 1, message = "patient id  should be greater than 0")
    private Long patientId;
    @NotBlank
    private String description;
    private String medicationName;
    private String dosage;

}
