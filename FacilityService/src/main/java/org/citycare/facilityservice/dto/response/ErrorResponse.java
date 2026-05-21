package org.citycare.facilityservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private boolean success; // Always false for errors
    private String message;
    private Map<String, String> errors; // For validation failures
    private long timestamp;
}
