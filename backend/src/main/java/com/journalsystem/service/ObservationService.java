package com.journalsystem.service;

import com.journalsystem.model.Observation;
import com.journalsystem.model.Patient;
import com.journalsystem.repository.ObservationRepository;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.service.fhir.ObservationFhirService;
import com.journalsystem.converter.ObservationFhirConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ObservationService {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private ObservationFhirService observationFhirService;
    
    @Autowired
    private ObservationFhirConverter observationFhirConverter;
    
    @Value("${fhir.enabled:false}")
    private boolean fhirEnabled;
    
    @Autowired
    private PatientService patientService;

    public List<Observation> getAllObservations() {
        return observationRepository.findAll();
    }

    public Observation getObservationById(Long id) {
        return observationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observation not found"));
    }

    public List<Observation> getObservationsByPatientId(Long patientId) {
        // First try local database
        Optional<Patient> localPatient = patientRepository.findById(patientId);
        if (localPatient.isPresent()) {
            return observationRepository.findByPatientOrderByObservationDateDesc(localPatient.get());
        }
        
        // If not found locally and FHIR is enabled, try FHIR
        if (fhirEnabled) {
            // Get the actual FHIR ID for this local ID
            String fhirId = patientService.getFhirIdForLocalId(patientId);
            if (fhirId == null) {
                // If no mapping found, try using the ID as-is
                fhirId = patientId.toString();
            }
            
            List<org.hl7.fhir.r4.model.Observation> fhirObservations = 
                    observationFhirService.getObservationsByPatient(fhirId);
            return fhirObservations.stream()
                    .map(fhirObservation -> {
                        Observation localObservation = observationFhirConverter.fromFhir(fhirObservation);
                        // Set a dummy patient with just the ID for reference
                        Patient patient = new Patient();
                        patient.setId(patientId);
                        localObservation.setPatient(patient);
                        return localObservation;
                    })
                    .collect(Collectors.toList());
        }
        
        // If FHIR is disabled or no results, return empty list
        return new ArrayList<>();
    }

    public Observation createObservation(Observation observation) {
        return observationRepository.save(observation);
    }

    public Observation updateObservation(Long id, Observation observationDetails) {
        Observation observation = getObservationById(id);

        if (observationDetails.getObservationType() != null) {
            observation.setObservationType(observationDetails.getObservationType());
        }
        if (observationDetails.getValue() != null) {
            observation.setValue(observationDetails.getValue());
        }
        if (observationDetails.getUnit() != null) {
            observation.setUnit(observationDetails.getUnit());
        }
        if (observationDetails.getNotes() != null) {
            observation.setNotes(observationDetails.getNotes());
        }

        return observationRepository.save(observation);
    }

    public void deleteObservation(Long id) {
        observationRepository.deleteById(id);
    }
}
