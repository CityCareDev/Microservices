package org.citycare.facilityservice.repositories;

import org.citycare.facilityservice.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByFacilityFacilityId(Long facilityId);
    List<Staff> findByRole(Staff.Role role);
    List<Staff> findByStatus(Staff.Status status);
//    Optional<Staff> findByUserId(Long userId);
}
