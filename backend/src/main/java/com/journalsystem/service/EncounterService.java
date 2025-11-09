package com.journalsystem.service;

import com.journalsystem.model.Encounter;
import com.journalsystem.model.Patient;
import com.journalsystem.model.Practitioner;
import com.journalsystem.repository.EncounterRepository;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.repository.PractitionerRepository;
import com.journalsystem.service.fhir.EncounterFhirService;
import com.journalsystem.converter.EncounterFhirConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EncounterService {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PractitionerRepository practitionerRepository;
    
    @Autowired
    private EncounterFhirService encounterFhirService;
    
    @Autowired
    private EncounterFhirConverter encounterFhirConverter;
    
    @Value("${fhir.enabled:false}")
    private boolean fhirEnabled;
    
    @Autowired
    private PatientService patientService;

    public List<Encounter> getAllEncounters() {
        return encounterRepository.findAll();
    }

    public Encounter getEncounterById(Long id) {
        return encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found"));
    }

    public List<Encounter> getEncountersByPatientId(Long patientId) {
        // First try local database
        Optional<Patient> localPatient = patientRepository.findById(patientId);
        if (localPatient.isPresent()) {
            return encounterRepository.findByPatientOrderByEncounterDateDesc(localPatient.get());
        }
        
        // If not found locally and FHIR is enabled, try FHIR
        if (fhirEnabled) {
            // Get the actual FHIR ID for this local ID
            String fhirId = patientService.getFhirIdForLocalId(patientId);
            if (fhirId == null) {
                // If no mapping found, try using the ID as-is
                fhirId = patientId.toString();
            }
            
            List<org.hl7.fhir.r4.model.Encounter> fhirEncounters = 
                    encounterFhirService.getEncountersByPatient(fhirId);
            return fhirEncounters.stream()
                    .map(fhirEncounter -> {
                        Encounter localEncounter = encounterFhirConverter.fromFhir(fhirEncounter);
                        // Set a dummy patient with just the ID for reference
                        Patient patient = new Patient();
                        patient.setId(patientId);
                        localEncounter.setPatient(patient);
                        return localEncounter;
                    })
                    .collect(Collectors.toList());
        }
        
        // If FHIR is disabled or no results, return empty list
        return new ArrayList<>();
    }

    public Encounter createEncounter(Encounter encounter) {
        return encounterRepository.save(encounter);
    }

    public Encounter updateEncounter(Long id, Encounter encounterDetails) {
        Encounter encounter = getEncounterById(id);

        if (encounterDetails.getEncounterType() != null) {
            encounter.setEncounterType(encounterDetails.getEncounterType());
        }
        if (encounterDetails.getStatus() != null) {
            encounter.setStatus(encounterDetails.getStatus());
        }
        if (encounterDetails.getNotes() != null) {
            encounter.setNotes(encounterDetails.getNotes());
        }
        if (encounterDetails.getReasonForVisit() != null) {
            encounter.setReasonForVisit(encounterDetails.getReasonForVisit());
        }

        return encounterRepository.save(encounter);
    }

    public void deleteEncounter(Long id) {
        encounterRepository.deleteById(id);
    }
}
