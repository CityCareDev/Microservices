package org.citycare.patienttreatmentservice.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdmitPatientRequest {
    @NotNull(message = "Emergency ID is required")
    @Positive(message = "Patient ID must be a valid positive number")
    @Min(value = 1, message = "emergency id  should be greater than 0")
    private Long citizenId;
    @NotNull(message = "Emergency ID is required")
    @Positive(message = "Patient ID must be a valid positive number")
    @Min(value = 1, message = "emergency id  should be greater than 0")
    private Long emergencyId;
    private Long facilityId;
    private String ward;
    private String notes;
}