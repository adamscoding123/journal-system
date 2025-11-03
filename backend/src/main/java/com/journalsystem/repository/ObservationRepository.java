package com.journalsystem.repository;

import com.journalsystem.model.Observation;
import com.journalsystem.model.Patient;
import com.journalsystem.model.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, Long> {
    List<Observation> findByPatient(Patient patient);
    List<Observation> findByEncounter(Encounter encounter);
    List<Observation> findByPatientOrderByObservationDateDesc(Patient patient);
}
