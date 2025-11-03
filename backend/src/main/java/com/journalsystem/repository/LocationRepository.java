package com.journalsystem.repository;

import com.journalsystem.model.Location;
import com.journalsystem.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByOrganization(Organization organization);
    List<Location> findByCity(String city);
}
