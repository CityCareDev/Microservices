package org.citycare.emergencyservice.repository;

import org.citycare.emergencyservice.entity.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByStatus(Ambulance.Status status);
    List<Ambulance> findByFacilityId(Long facilityId);
    List<Ambulance> findByFacilityIdAndStatus(Long facilityId, Ambulance.Status status);
}