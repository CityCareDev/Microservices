package org.citycare.citizenservice.repository;


import org.citycare.citizenservice.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CitizenRepository extends JpaRepository<Citizen, Long> {

}
