package org.citycare.patienttreatmentservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Treatment {

    public enum Status { ONGOING, COMPLETED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long treatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private org.citycare.patienttreatmentservice.entity.Patient patient;

    /** assignedById from auth-service — cross-service reference */
    @Column(nullable = false)
    private Long assignedById;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String medicationName;
    private String dosage;

    @Builder.Default
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    @Builder.Default private Status status = Status.ONGOING;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
