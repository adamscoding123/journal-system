package com.journalsystem.controller;

import com.journalsystem.service.fhir.*;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fhir-test")
public class FhirTestController {
    
    @Autowired
    private PatientFhirService patientFhirService;
    
    @Autowired
    private ObservationFhirService observationFhirService;
    
    @Autowired
    private EncounterFhirService encounterFhirService;
    
    @Autowired
    private ConditionFhirService conditionFhirService;
    
    @Autowired
    private PractitionerFhirService practitionerFhirService;
    
    @Autowired
    private OrganizationFhirService organizationFhirService;
    
    @Autowired
    private LocationFhirService locationFhirService;
    
    /**
     * Test endpoint to get FHIR server status and available resources
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFhirServerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Get counts of each resource type
            status.put("status", "connected");
            status.put("serverUrl", "https://hapi-fhir.app.cloud.cbh.kth.se/fhir");
            
            Map<String, Integer> resourceCounts = new HashMap<>();
            resourceCounts.put("patients", patientFhirService.getAllPatients().size());
            resourceCounts.put("observations", observationFhirService.getAllObservations().size());
            resourceCounts.put("encounters", encounterFhirService.getAllEncounters().size());
            resourceCounts.put("conditions", conditionFhirService.getAllConditions().size());
            resourceCounts.put("practitioners", practitionerFhirService.getAllPractitioners().size());
            resourceCounts.put("organizations", organizationFhirService.getAllOrganizations().size());
            resourceCounts.put("locations", locationFhirService.getAllLocations().size());
            
            status.put("resourceCounts", resourceCounts);
            
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get all patients from FHIR server
     */
    @GetMapping("/patients")
    public ResponseEntity<List<Map<String, Object>>> getFhirPatients() {
        List<Patient> patients = patientFhirService.getAllPatients();
        
        List<Map<String, Object>> patientData = patients.stream()
                .map(patient -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", patient.getId());
                    
                    if (patient.hasName() && !patient.getName().isEmpty()) {
                        HumanName name = patient.getName().get(0);
                        data.put("firstName", name.getGivenAsSingleString());
                        data.put("lastName", name.getFamily());
                    }
                    
                    if (patient.hasBirthDate()) {
                        data.put("birthDate", patient.getBirthDate());
                    }
                    
                    // Get personal number from identifier
                    patient.getIdentifier().stream()
                            .filter(id -> "http://electronichealth.se/identifier/personnummer".equals(id.getSystem()))
                            .findFirst()
                            .ifPresent(id -> data.put("personalNumber", id.getValue()));
                    
                    return data;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(patientData);
    }
    
    /**
     * Get patient details including related resources
     */
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String patientId) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Get patient
            patientFhirService.getPatientById(patientId).ifPresent(patient -> {
                Map<String, Object> patientData = new HashMap<>();
                patientData.put("id", patient.getId());
                
                if (patient.hasName() && !patient.getName().isEmpty()) {
                    HumanName name = patient.getName().get(0);
                    patientData.put("name", name.getNameAsSingleString());
                }
                
                details.put("patient", patientData);
            });
            
            // Get related observations
            List<Observation> observations = observationFhirService.getObservationsByPatient(patientId);
            details.put("observationCount", observations.size());
            
            // Get related encounters
            List<Encounter> encounters = encounterFhirService.getEncountersByPatient(patientId);
            details.put("encounterCount", encounters.size());
            
            // Get related conditions
            List<Condition> conditions = conditionFhirService.getConditionsByPatient(patientId);
            details.put("conditionCount", conditions.size());
            
        } catch (Exception e) {
            details.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(details);
    }
    
    /**
     * Find patients with data (encounters, observations, conditions)
     */
    @GetMapping("/patients-with-data")
    public ResponseEntity<Map<String, Object>> findPatientsWithData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // First, let's see what data exists in FHIR
            List<Observation> allObservations = observationFhirService.getAllObservations();
            List<Encounter> allEncounters = encounterFhirService.getAllEncounters();
            List<Condition> allConditions = conditionFhirService.getAllConditions();
            
            result.put("totalObservations", allObservations.size());
            result.put("totalEncounters", allEncounters.size());
            result.put("totalConditions", allConditions.size());
            
            // Get unique patient references from observations
            List<String> patientsWithData = new ArrayList<>();
            Map<String, Map<String, Integer>> patientDataCounts = new HashMap<>();
            
            // From observations
            for (Observation obs : allObservations.subList(0, Math.min(100, allObservations.size()))) {
                if (obs.hasSubject() && obs.getSubject().hasReference()) {
                    String patientRef = obs.getSubject().getReference();
                    String patientId = patientRef.replace("Patient/", "");
                    
                    patientDataCounts.computeIfAbsent(patientId, k -> new HashMap<>());
                    patientDataCounts.get(patientId).merge("observations", 1, Integer::sum);
                    
                    if (!patientsWithData.contains(patientId)) {
                        patientsWithData.add(patientId);
                    }
                }
            }
            
            // From encounters
            for (Encounter enc : allEncounters.subList(0, Math.min(100, allEncounters.size()))) {
                if (enc.hasSubject() && enc.getSubject().hasReference()) {
                    String patientRef = enc.getSubject().getReference();
                    String patientId = patientRef.replace("Patient/", "");
                    
                    patientDataCounts.computeIfAbsent(patientId, k -> new HashMap<>());
                    patientDataCounts.get(patientId).merge("encounters", 1, Integer::sum);
                    
                    if (!patientsWithData.contains(patientId)) {
                        patientsWithData.add(patientId);
                    }
                }
            }
            
            // From conditions
            for (Condition cond : allConditions.subList(0, Math.min(100, allConditions.size()))) {
                if (cond.hasSubject() && cond.getSubject().hasReference()) {
                    String patientRef = cond.getSubject().getReference();
                    String patientId = patientRef.replace("Patient/", "");
                    
                    patientDataCounts.computeIfAbsent(patientId, k -> new HashMap<>());
                    patientDataCounts.get(patientId).merge("conditions", 1, Integer::sum);
                    
                    if (!patientsWithData.contains(patientId)) {
                        patientsWithData.add(patientId);
                    }
                }
            }
            
            result.put("patientsWithData", patientsWithData.subList(0, Math.min(10, patientsWithData.size())));
            result.put("patientDataCounts", patientDataCounts);
            
            // Try to get patient details for the first few
            List<Map<String, Object>> patientDetails = new ArrayList<>();
            for (int i = 0; i < Math.min(5, patientsWithData.size()); i++) {
                String patientId = patientsWithData.get(i);
                Map<String, Object> details = new HashMap<>();
                details.put("fhirId", patientId);
                details.put("dataCounts", patientDataCounts.get(patientId));
                
                // Try to get patient info
                patientFhirService.getPatientById(patientId).ifPresent(patient -> {
                    if (!patient.getName().isEmpty()) {
                        details.put("name", patient.getName().get(0).getNameAsSingleString());
                    }
                    patient.getIdentifier().stream()
                            .filter(id -> id.getSystem() != null && id.getSystem().contains("personnummer"))
                            .findFirst()
                            .ifPresent(id -> details.put("personalNumber", id.getValue()));
                });
                
                patientDetails.add(details);
            }
            
            result.put("patientDetails", patientDetails);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getStackTrace());
        }
        
        return ResponseEntity.ok(result);
    }
}