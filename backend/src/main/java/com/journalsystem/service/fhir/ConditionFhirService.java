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
public class ConditionFhirService {
    
    @Autowired
    private IGenericClient fhirClient;
    
    @Autowired
    private FhirContext fhirContext;
    
    public Optional<Condition> getConditionById(String id) {
        try {
            Condition condition = fhirClient
                    .read()
                    .resource(Condition.class)
                    .withId(id)
                    .execute();
            return Optional.of(condition);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Condition> getConditionsByPatient(String patientId) {
        Bundle bundle = fhirClient
                .search()
                .forResource(Condition.class)
                .where(Condition.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Condition) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public List<Condition> getConditionsByEncounter(String encounterId) {
        Bundle bundle = fhirClient
                .search()
                .forResource(Condition.class)
                .where(Condition.ENCOUNTER.hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Condition) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public List<Condition> getAllConditions() {
        Bundle bundle = fhirClient
                .search()
                .forResource(Condition.class)
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Condition) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public Condition createCondition(Condition condition) {
        return (Condition) fhirClient
                .create()
                .resource(condition)
                .execute()
                .getResource();
    }
    
    public Condition updateCondition(Condition condition) {
        return (Condition) fhirClient
                .update()
                .resource(condition)
                .execute()
                .getResource();
    }
    
    public void deleteCondition(String id) {
        fhirClient
                .delete()
                .resourceById("Condition", id)
                .execute();
    }
}