package com.journalsystem.repository;

import com.journalsystem.model.Condition;
import com.journalsystem.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionRepository extends JpaRepository<Condition, Long> {
    List<Condition> findByPatient(Patient patient);
    List<Condition> findByPatientOrderByDiagnosisDateDesc(Patient patient);
}
