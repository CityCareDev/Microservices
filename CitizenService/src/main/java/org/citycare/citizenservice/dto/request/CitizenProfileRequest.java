package org.citycare.citizenservice.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.citycare.citizenservice.entity.Citizen;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class CitizenProfileRequest {

    @JsonIgnore
    private final Set<String> providedFields = new HashSet<>();

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @JsonFormat(pattern = "dd-MM-yyyy", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDate dateOfBirth;

    private Citizen.Gender gender;

    private String address;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String contactInfo;

    @JsonIgnore
    public boolean hasField(String fieldName) {
        return providedFields.contains(fieldName);
    }

    @JsonSetter("name")
    public void setName(String name) {
        providedFields.add("name");
        this.name = name;
    }

    @JsonSetter("dateOfBirth")
    public void setDateOfBirth(LocalDate dateOfBirth) {
        providedFields.add("dateOfBirth");
        this.dateOfBirth = dateOfBirth;
    }

    @JsonSetter("gender")
    public void setGender(Citizen.Gender gender) {
        providedFields.add("gender");
        this.gender = gender;
    }

    @JsonSetter("address")
    public void setAddress(String address) {
        providedFields.add("address");
        this.address = address;
    }

    @JsonSetter("contactInfo")
    public void setContactInfo(String contactInfo) {
        providedFields.add("contactInfo");
        this.contactInfo = contactInfo;
    }
}
