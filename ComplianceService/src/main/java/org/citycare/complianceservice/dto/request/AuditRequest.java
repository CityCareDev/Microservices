package org.citycare.complianceservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AuditRequest {
    @NotBlank
    private String scope;

    @NotNull
    private LocalDate date;

    private String findings;
}
