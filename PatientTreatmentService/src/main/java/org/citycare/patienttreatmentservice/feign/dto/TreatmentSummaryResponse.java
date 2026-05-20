package org.citycare.patienttreatmentservice.feign.dto;

import lombok.*;
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