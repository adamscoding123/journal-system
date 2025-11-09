package com.journalsystem.service.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EncounterFhirService {
    
    @Autowired
    private IGenericClient fhirClient;
    
    @Autowired
    private FhirContext fhirContext;
    
    public Optional<Encounter> getEncounterById(String id) {
        try {
            Encounter encounter = fhirClient
                    .read()
                    .resource(Encounter.class)
                    .withId(id)
                    .execute();
            return Optional.of(encounter);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Encounter> getEncountersByPatient(String patientId) {
        Bundle bundle = fhirClient
                .search()
                .forResource(Encounter.class)
                .where(Encounter.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Encounter) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public List<Encounter> getEncountersByPractitioner(String practitionerId) {
        Bundle bundle = fhirClient
                .search()
                .forResource(Encounter.class)
                .where(Encounter.PARTICIPANT.hasId(practitionerId))
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Encounter) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public List<Encounter> getAllEncounters() {
        Bundle bundle = fhirClient
                .search()
                .forResource(Encounter.class)
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Encounter) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public Encounter createEncounter(Encounter encounter) {
        return (Encounter) fhirClient
                .create()
                .resource(encounter)
                .execute()
                .getResource();
    }
    
    public Encounter updateEncounter(Encounter encounter) {
        return (Encounter) fhirClient
                .update()
                .resource(encounter)
                .execute()
                .getResource();
    }
    
    public void deleteEncounter(String id) {
        fhirClient
                .delete()
                .resourceById("Encounter", id)
                .execute();
    }
}