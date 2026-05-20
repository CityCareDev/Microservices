package org.citycare.emergencyservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class Emergency {

    public enum Type { ACCIDENT, HEART_ATTACK, FIRE, STROKE, FALL, OTHER }
    public enum Status { REPORTED, DISPATCHED, ADMITTED, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emergencyId;

    /** citizenId references auth-service user — no FK across services */
    @Column(nullable = false)
    private Long citizenId;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Type type;

    @NotBlank @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    @Builder.Default private Status status = Status.REPORTED;

    /** dispatcherId references auth-service user */
    private Long dispatcherId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulance_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Ambulance ambulance;

    private LocalDateTime dispatchedAt;

    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
