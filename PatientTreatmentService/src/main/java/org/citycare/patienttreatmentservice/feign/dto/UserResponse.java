package org.citycare.patienttreatmentservice.feign.dto;
import lombok.Data;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;   // Enum ni String ga receive cheskovadam best
    private String phone;
    private String status;
}
