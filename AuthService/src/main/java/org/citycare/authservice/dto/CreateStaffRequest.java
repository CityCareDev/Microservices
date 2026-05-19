package org.citycare.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.citycare.authservice.entity.User;

@Data
public class CreateStaffRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String password;
    @Pattern(regexp = "^\\+?[0-9][0-9\\-\\s]{8,13}[0-9]$", message = "Invalid phone number format") private String phone;
    private User.Role role;
}
