package com.journalsystem.service.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for interacting with Organization resources in the FHIR server
 */
@Service
public class OrganizationFhirService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationFhirService.class);
    
    @Autowired
    private IGenericClient fhirClient;
    
    /**
     * Get all organizations from FHIR server
     */
    public List<Organization> getAllOrganizations() {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Organization.class)
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Organization> organizations = new ArrayList<>();
            bundle.getEntry().forEach(entry -> {
                if (entry.getResource() instanceof Organization) {
                    organizations.add((Organization) entry.getResource());
                }
            });
            
            logger.info("Retrieved {} organizations from FHIR server", organizations.size());
            return organizations;
        } catch (Exception e) {
            logger.error("Error fetching organizations from FHIR server", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get organization by ID from FHIR server
     */
    public Optional<Organization> getOrganizationById(String id) {
        try {
            Organization organization = (Organization) fhirClient.read()
                    .resource(Organization.class)
                    .withId(id)
                    .execute();
            return Optional.of(organization);
        } catch (Exception e) {
            logger.error("Error fetching organization with ID {} from FHIR server", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Create a new organization in FHIR server
     */
    public Organization createOrganization(Organization organization) {
        try {
            MethodOutcome outcome = fhirClient.create()
                    .resource(organization)
                    .execute();
            
            return (Organization) outcome.getResource();
        } catch (Exception e) {
            logger.error("Error creating organization in FHIR server", e);
            throw new RuntimeException("Failed to create organization in FHIR server", e);
        }
    }
    
    /**
     * Update an organization in FHIR server
     */
    public Organization updateOrganization(Organization organization) {
        try {
            MethodOutcome outcome = fhirClient.update()
                    .resource(organization)
                    .execute();
            
            return (Organization) outcome.getResource();
        } catch (Exception e) {
            logger.error("Error updating organization in FHIR server", e);
            throw new RuntimeException("Failed to update organization in FHIR server", e);
        }
    }
    
    /**
     * Delete an organization from FHIR server
     */
    public void deleteOrganization(String id) {
        try {
            fhirClient.delete()
                    .resourceById("Organization", id)
                    .execute();
            
            logger.info("Deleted organization with ID {} from FHIR server", id);
        } catch (Exception e) {
            logger.error("Error deleting organization with ID {} from FHIR server", id, e);
            throw new RuntimeException("Failed to delete organization from FHIR server", e);
        }
    }
    
    /**
     * Search organizations by name
     */
    public List<Organization> searchOrganizationsByName(String name) {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Organization.class)
                    .where(Organization.NAME.matches().value(name))
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Organization> organizations = new ArrayList<>();
            bundle.getEntry().forEach(entry -> {
                if (entry.getResource() instanceof Organization) {
                    organizations.add((Organization) entry.getResource());
                }
            });
            
            return organizations;
        } catch (Exception e) {
            logger.error("Error searching organizations by name: {}", name, e);
            return new ArrayList<>();
        }
    }
}