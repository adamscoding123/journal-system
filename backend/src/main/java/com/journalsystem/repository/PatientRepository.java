package com.journalsystem.repository;

import com.journalsystem.model.Patient;
import com.journalsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByPersonalNumber(String personalNumber);
    Optional<Patient> findByUserId(Long userId);
}
