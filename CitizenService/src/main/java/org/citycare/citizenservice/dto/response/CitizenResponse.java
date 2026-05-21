package org.citycare.citizenservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenResponse {

    private Long citizenId;
    private String name;
    private String contactInfo;
    private String status;
}