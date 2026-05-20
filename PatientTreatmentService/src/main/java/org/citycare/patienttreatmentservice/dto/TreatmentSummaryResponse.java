package org.citycare.patienttreatmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentSummaryResponse {
    private Long treatmentId;
    private Long patientId;
    private String description;
    private String medicationName;
    private String dosage;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}