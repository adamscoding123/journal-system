package com.journalsystem.service;

import com.journalsystem.model.Condition;
import com.journalsystem.model.Patient;
import com.journalsystem.repository.ConditionRepository;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.service.fhir.ConditionFhirService;
import com.journalsystem.converter.ConditionFhirConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConditionService {

    @Autowired
    private ConditionRepository conditionRepository;

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private ConditionFhirService conditionFhirService;
    
    @Autowired
    private ConditionFhirConverter conditionFhirConverter;
    
    @Value("${fhir.enabled:false}")
    private boolean fhirEnabled;
    
    @Autowired
    private PatientService patientService;

    public List<Condition> getAllConditions() {
        return conditionRepository.findAll();
    }

    public Condition getConditionById(Long id) {
        return conditionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condition not found"));
    }

    public List<Condition> getConditionsByPatientId(Long patientId) {
        // First try local database
        Optional<Patient> localPatient = patientRepository.findById(patientId);
        if (localPatient.isPresent()) {
            return conditionRepository.findByPatientOrderByDiagnosisDateDesc(localPatient.get());
        }
        
        // If not found locally and FHIR is enabled, try FHIR
        if (fhirEnabled) {
            // Get the actual FHIR ID for this local ID
            String fhirId = patientService.getFhirIdForLocalId(patientId);
            if (fhirId == null) {
                // If no mapping found, try using the ID as-is
                fhirId = patientId.toString();
            }
            
            List<org.hl7.fhir.r4.model.Condition> fhirConditions = 
                    conditionFhirService.getConditionsByPatient(fhirId);
            return fhirConditions.stream()
                    .map(fhirCondition -> {
                        Condition localCondition = conditionFhirConverter.fromFhir(fhirCondition);
                        // Set a dummy patient with just the ID for reference
                        Patient patient = new Patient();
                        patient.setId(patientId);
                        localCondition.setPatient(patient);
                        return localCondition;
                    })
                    .collect(Collectors.toList());
        }
        
        // If FHIR is disabled or no results, return empty list
        return new ArrayList<>();
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
