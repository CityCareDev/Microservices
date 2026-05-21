package org.citycare.facilityservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff extends BaseEntity {

    public enum Role {
        DOCTOR, DISPATCHER, COMPLIANCE_OFFICER
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    // Link to User account
    @Column(name = "user_id", nullable = false)
    private Long userId;
}
