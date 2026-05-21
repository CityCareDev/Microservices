package org.citycare.facilityservice.dto.response;

import lombok.*;
import org.citycare.facilityservice.entities.Staff;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long staffId;
    private String name;
    private Staff.Role role;
    private String contactInfo;
    private Staff.Status status;
    private Long facilityId;
    private Long userId;
}
