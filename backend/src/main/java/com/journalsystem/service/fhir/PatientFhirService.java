package com.journalsystem.service.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientFhirService {
    
    @Autowired
    private IGenericClient fhirClient;
    
    @Autowired
    private FhirContext fhirContext;
    
    public Optional<Patient> getPatientById(String id) {
        try {
            Patient patient = fhirClient
                    .read()
                    .resource(Patient.class)
                    .withId(id)
                    .execute();
            return Optional.of(patient);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public Optional<Patient> getPatientByPersonalNumber(String personalNumber) {
        try {
            Bundle bundle = fhirClient
                    .search()
                    .forResource(Patient.class)
                    .where(Patient.IDENTIFIER.exactly().systemAndCode("http://electronichealth.se/identifier/personnummer", personalNumber))
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Patient> patients = BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                    .map(entry -> (Patient) entry.getResource())
                    .collect(Collectors.toList());
            
            return patients.isEmpty() ? Optional.empty() : Optional.of(patients.get(0));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Patient> getAllPatients() {
        List<Patient> allPatients = new ArrayList<>();
        
        // First page - get 100 at a time for efficiency
        Bundle bundle = fhirClient
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .count(100)
                .execute();
        
        // Add patients from first page
        allPatients.addAll(BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Patient) entry.getResource())
                .collect(Collectors.toList()));
        
        // Follow pagination links to get all remaining pages
        while (bundle.getLink(Bundle.LINK_NEXT) != null) {
            bundle = fhirClient
                    .loadPage()
                    .next(bundle)
                    .execute();
            
            allPatients.addAll(BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                    .map(entry -> (Patient) entry.getResource())
                    .collect(Collectors.toList()));
        }
        
        return allPatients;
    }
    
    public Patient createPatient(Patient patient) {
        return (Patient) fhirClient
                .create()
                .resource(patient)
                .execute()
                .getResource();
    }
    
    public Patient updatePatient(Patient patient) {
        return (Patient) fhirClient
                .update()
                .resource(patient)
                .execute()
                .getResource();
    }
    
    public void deletePatient(String id) {
        fhirClient
                .delete()
                .resourceById("Patient", id)
                .execute();
    }
}