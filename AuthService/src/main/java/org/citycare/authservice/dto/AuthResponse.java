package org.citycare.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.citycare.authservice.entity.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long userId;
    private String name;
    private String email;
    private User.Role role;
    private String token;
}
