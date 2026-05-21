package org.citycare.facilityservice.repositories;

import org.citycare.facilityservice.entities.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findByType(Facility.Type type);
    List<Facility> findByStatus(Facility.Status status);
}
