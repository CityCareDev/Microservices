package org.citycare.patienttreatmentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Patient {

    public enum Status { ADMITTED, UNDER_OBSERVATION, STABLE, DISCHARGED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    /** citizenId from citizen-service — cross-service reference, no FK */
    @Column(nullable = false)
    private Long citizenId;

    /** emergencyId from emergency-service — cross-service reference */
    @Column(nullable = false, unique = true)
    private Long emergencyId;

    @Column(name = "facility_id")
    private Long facilityId;

    /** staffId from facility-service — set when staff first adds a treatment */
    @Column(name = "assigned_staff_id")
    private Long assignedStaffId;

    @Column(nullable = false)
    private LocalDate admissionDate;

    private LocalDate dischargeDate;
    private String ward;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    @Builder.Default private Status status = Status.ADMITTED;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
