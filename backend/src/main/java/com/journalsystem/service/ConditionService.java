package com.journalsystem.service;

import com.journalsystem.model.Condition;
import com.journalsystem.model.Patient;
import com.journalsystem.repository.ConditionRepository;
import com.journalsystem.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConditionService {

    @Autowired
    private ConditionRepository conditionRepository;

    @Autowired
    private PatientRepository patientRepository;

    public List<Condition> getAllConditions() {
        return conditionRepository.findAll();
    }

    public Condition getConditionById(Long id) {
        return conditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condition not found"));
    }

    public List<Condition> getConditionsByPatientId(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return conditionRepository.findByPatientOrderByDiagnosisDateDesc(patient);
    }

    public Condition createCondition(Condition condition) {
        return conditionRepository.save(condition);
    }

    public Condition updateCondition(Long id, Condition conditionDetails) {
        Condition condition = getConditionById(id);

        if (conditionDetails.getDiagnosis() != null) {
            condition.setDiagnosis(conditionDetails.getDiagnosis());
        }
        if (conditionDetails.getCode() != null) {
            condition.setCode(conditionDetails.getCode());
        }
        if (conditionDetails.getSeverity() != null) {
            condition.setSeverity(conditionDetails.getSeverity());
        }
        if (conditionDetails.getStatus() != null) {
            condition.setStatus(conditionDetails.getStatus());
        }
        if (conditionDetails.getNotes() != null) {
            condition.setNotes(conditionDetails.getNotes());
        }

        return conditionRepository.save(condition);
    }

    public void deleteCondition(Long id) {
        conditionRepository.deleteById(id);
    }
}
