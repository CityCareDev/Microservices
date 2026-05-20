package org.citycare.emergencyservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ambulances", uniqueConstraints = @UniqueConstraint(columnNames = "vehicle_number"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class Ambulance {

    public enum Status
    { AVAILABLE, DISPATCHED, MAINTENANCE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ambulanceId;

    @NotBlank
    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String vehicleNumber;

    private String model;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    @Builder.Default private Status status = Status.AVAILABLE;

    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

