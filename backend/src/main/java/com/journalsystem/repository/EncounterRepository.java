package com.journalsystem.repository;

import com.journalsystem.model.Encounter;
import com.journalsystem.model.Patient;
import com.journalsystem.model.Practitioner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {
    List<Encounter> findByPatient(Patient patient);
    List<Encounter> findByPractitioner(Practitioner practitioner);
    List<Encounter> findByPatientOrderByEncounterDateDesc(Patient patient);
}
