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
public class PractitionerFhirService {
    
    @Autowired
    private IGenericClient fhirClient;
    
    @Autowired
    private FhirContext fhirContext;
    
    public Optional<Practitioner> getPractitionerById(String id) {
        try {
            Practitioner practitioner = fhirClient
                    .read()
                    .resource(Practitioner.class)
                    .withId(id)
                    .execute();
            return Optional.of(practitioner);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public Optional<Practitioner> getPractitionerByLicenseNumber(String licenseNumber) {
        try {
            Bundle bundle = fhirClient
                    .search()
                    .forResource(Practitioner.class)
                    .where(Practitioner.IDENTIFIER.exactly().identifier(licenseNumber))
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Practitioner> practitioners = BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                    .map(entry -> (Practitioner) entry.getResource())
                    .collect(Collectors.toList());
            
            return practitioners.isEmpty() ? Optional.empty() : Optional.of(practitioners.get(0));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<Practitioner> getAllPractitioners() {
        Bundle bundle = fhirClient
                .search()
                .forResource(Practitioner.class)
                .returnBundle(Bundle.class)
                .execute();
        
        return BundleUtil.toListOfEntries(fhirContext, bundle).stream()
                .map(entry -> (Practitioner) entry.getResource())
                .collect(Collectors.toList());
    }
    
    public Practitioner createPractitioner(Practitioner practitioner) {
        return (Practitioner) fhirClient
                .create()
                .resource(practitioner)
                .execute()
                .getResource();
    }
    
    public Practitioner updatePractitioner(Practitioner practitioner) {
        return (Practitioner) fhirClient
                .update()
                .resource(practitioner)
                .execute()
                .getResource();
    }
    
    public void deletePractitioner(String id) {
        fhirClient
                .delete()
                .resourceById("Practitioner", id)
                .execute();
    }
}