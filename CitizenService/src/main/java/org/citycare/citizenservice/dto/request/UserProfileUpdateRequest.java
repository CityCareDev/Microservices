package org.citycare.citizenservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Contact info must not be blank")
    private String contactInfo;
}