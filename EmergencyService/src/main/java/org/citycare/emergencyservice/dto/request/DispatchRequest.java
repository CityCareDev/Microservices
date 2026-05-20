package org.citycare.emergencyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispatchRequest {
    @NotNull private Long ambulanceId;
}
