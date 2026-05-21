package org.citycare.complianceservice.dto.request;

import org.citycare.complianceservice.entity.ComplianceRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ComplianceRecordRequest {
    @NotNull
    private Long entityId;

    @NotNull
    private ComplianceRecord.EntityType type;

    @NotNull
    private ComplianceRecord.Result result;

    @NotNull
    private LocalDate date;

    private String notes;
}
