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
public class ObservationFhirService {
    
    @Autowired
    private IGenericClient fhirClient;
    
    @Autowired
    private FhirContext fhirContext;
    
    public Optional<Observation> getObservationById(String id) {
        try {
            Observation observation = fhirClient
                    .read()
                    .resource(Observation.class)
                    .withId(id)
                    .execute();
            return Optional.of(observation);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Observation> getObservationsByPatient(String patientId) {
        Bundle bundle = fhirClient
                .search()
                .forResource(Observation.class)
                .where(Observation.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Observation) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public List<Observation> getObservationsByEncounter(String encounterId) {
        Bundle bundle = fhirClient
                .search()
                .forResource(Observation.class)
                .where(Observation.ENCOUNTER.hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Observation) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public List<Observation> getAllObservations() {
        Bundle bundle = fhirClient
                .search()
                .forResource(Observation.class)
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Observation) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public Observation createObservation(Observation observation) {
        return (Observation) fhirClient
                .create()
                .resource(observation)
                .execute()
                .getResource();
    }
    
    public Observation updateObservation(Observation observation) {
        return (Observation) fhirClient
                .update()
                .resource(observation)
                .execute()
                .getResource();
    }
    
    public void deleteObservation(String id) {
        fhirClient
                .delete()
                .resourceById("Observation", id)
                .execute();
    }
}