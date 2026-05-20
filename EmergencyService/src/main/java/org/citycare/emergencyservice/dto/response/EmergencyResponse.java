package org.citycare.emergencyservice.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyResponse {

    private Long emergencyId;
    private Long citizenId;
    private String status;
    private Long ambulanceId;
}