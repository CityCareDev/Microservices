package org.citycare.emergencyservice.repository;

import org.citycare.emergencyservice.entity.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    List<Emergency> findByStatusOrderByCreatedAtDesc(Emergency.Status status);
    List<Emergency> findByStatus(Emergency.Status status);
    List<Emergency> findByCitizenId(Long citizenId);
    List<Emergency> findByDispatcherIdOrderByDispatchedAtDesc(Long dispatcherId);
}