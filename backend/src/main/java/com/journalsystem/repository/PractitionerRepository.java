package com.journalsystem.repository;

import com.journalsystem.model.Practitioner;
import com.journalsystem.model.User;
import com.journalsystem.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PractitionerRepository extends JpaRepository<Practitioner, Long> {
    Optional<Practitioner> findByUser(User user);
    Optional<Practitioner> findByUserId(Long userId);
    List<Practitioner> findByOrganization(Organization organization);
}
