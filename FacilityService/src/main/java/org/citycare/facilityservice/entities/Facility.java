package org.citycare.facilityservice.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility extends org.citycare.facilityservice.entities.BaseEntity {

    public enum Type {
        HOSPITAL, CLINIC
    }

    public enum Status {
        ACTIVE, INACTIVE, MAINTENANCE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facilityId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;
}
