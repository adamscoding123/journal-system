package com.journalsystem.service;

import com.journalsystem.model.Patient;
import com.journalsystem.model.User;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.repository.UserRepository;
import com.journalsystem.service.fhir.PatientFhirService;
import com.journalsystem.converter.PatientFhirConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PatientFhirService patientFhirService;
    
    @Autowired
    private PatientFhirConverter patientFhirConverter;
    
    @Value("${fhir.enabled:false}")
    private boolean fhirEnabled;
    
    // Cache to map local IDs to FHIR IDs
    private static final Map<Long, String> localIdToFhirIdMap = new HashMap<>();
    
    public String getFhirIdForLocalId(Long localId) {
        return localIdToFhirIdMap.get(localId);
    }

    public List<Patient> getAllPatients() {
        if (fhirEnabled) {
            // Get patients from FHIR server
            List<org.hl7.fhir.r4.model.Patient> fhirPatients = patientFhirService.getAllPatients();
            return fhirPatients.stream()
                    .map(fhirPatient -> {
                        Patient localPatient = patientFhirConverter.fromFhir(fhirPatient);
                        
                        // Store the mapping between local ID and FHIR ID
                        if (localPatient.getId() != null && fhirPatient.hasIdElement()) {
                            localIdToFhirIdMap.put(localPatient.getId(), fhirPatient.getIdElement().getIdPart());
                        }
                        
                        // Try to match with local user by personal number
                        if (localPatient.getPersonalNumber() != null) {
                            patientRepository.findByPersonalNumber(localPatient.getPersonalNumber())
                                    .ifPresent(existingPatient -> {
                                        localPatient.setUser(existingPatient.getUser());
                                        localPatient.setId(existingPatient.getId());
                                    });
                        }
                        return localPatient;
                    })
                    .collect(Collectors.toList());
        }
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        if (fhirEnabled) {
            // Try FHIR first
            Optional<org.hl7.fhir.r4.model.Patient> fhirPatient = patientFhirService.getPatientById(id.toString());
            if (fhirPatient.isPresent()) {
                Patient localPatient = patientFhirConverter.fromFhir(fhirPatient.get());
                // Get local patient for user association
                patientRepository.findById(id).ifPresent(existingPatient -> {
                    localPatient.setUser(existingPatient.getUser());
                    localPatient.setId(existingPatient.getId());
                });
                return localPatient;
            }
        }
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user"));
    }

    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);

        if (patientDetails.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientDetails.getDateOfBirth());
        }
        if (patientDetails.getAddress() != null) {
            patient.setAddress(patientDetails.getAddress());
        }
        if (patientDetails.getPhoneNumber() != null) {
            patient.setPhoneNumber(patientDetails.getPhoneNumber());
        }
        if (patientDetails.getBloodType() != null) {
            patient.setBloodType(patientDetails.getBloodType());
        }
        if (patientDetails.getAllergies() != null) {
            patient.setAllergies(patientDetails.getAllergies());
        }
        if (patientDetails.getMedications() != null) {
            patient.setMedications(patientDetails.getMedications());
        }

        // Save locally first
        Patient savedPatient = patientRepository.save(patient);
        
        // If FHIR is enabled, also update in FHIR server
        if (fhirEnabled) {
            org.hl7.fhir.r4.model.Patient fhirPatient = patientFhirConverter.toFhir(savedPatient);
            patientFhirService.updatePatient(fhirPatient);
        }
        
        return savedPatient;
    }

    public void deletePatient(Long id) {
        // Delete from FHIR if enabled
        if (fhirEnabled) {
            patientFhirService.deletePatient(id.toString());
        }
        // Always delete locally
        patientRepository.deleteById(id);
    }
    
    /**
     * Create a new patient - used during registration
     */
    public Patient createPatient(Patient patient) {
        // Save locally first
        Patient savedPatient = patientRepository.save(patient);
        
        // If FHIR is enabled, also create in FHIR server
        if (fhirEnabled) {
            org.hl7.fhir.r4.model.Patient fhirPatient = patientFhirConverter.toFhir(savedPatient);
            patientFhirService.createPatient(fhirPatient);
        }
        
        return savedPatient;
    }
}
