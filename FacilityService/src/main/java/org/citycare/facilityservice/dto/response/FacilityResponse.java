package org.citycare.facilityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.citycare.facilityservice.entities.Facility;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityResponse {

    private Long facilityId; // Essential for the frontend to perform Edit/Delete

    private String name;

    private Facility.Type type; // Keeping it as Enum is fine, or convert to String

    private String location;

    private int capacity;

    private Facility.Status status;

}