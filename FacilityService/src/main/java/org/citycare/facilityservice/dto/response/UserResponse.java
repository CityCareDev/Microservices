package org.citycare.facilityservice.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long userId;    // The most important field (The "501" link)

    private String name;    // To display on the Staff profile

    private String email;   // To contact the staff member

    private String role;    // To verify if they are DOCTOR, DISPATCHER, etc.

    private String phone;   // For emergency contact

    private String status;  // ACTIVE or INACTIVE
}
