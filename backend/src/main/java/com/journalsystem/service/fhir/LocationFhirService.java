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
 * Service class for interacting with Location resources in the FHIR server
 */
@Service
public class LocationFhirService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationFhirService.class);
    
    @Autowired
    private IGenericClient fhirClient;
    
    /**
     * Get all locations from FHIR server
     */
    public List<Location> getAllLocations() {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Location.class)
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Location> locations = new ArrayList<>();
            bundle.getEntry().forEach(entry -> {
                if (entry.getResource() instanceof Location) {
                    locations.add((Location) entry.getResource());
                }
            });
            
            logger.info("Retrieved {} locations from FHIR server", locations.size());
            return locations;
        } catch (Exception e) {
            logger.error("Error fetching locations from FHIR server", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get location by ID from FHIR server
     */
    public Optional<Location> getLocationById(String id) {
        try {
            Location location = (Location) fhirClient.read()
                    .resource(Location.class)
                    .withId(id)
                    .execute();
            return Optional.of(location);
        } catch (Exception e) {
            logger.error("Error fetching location with ID {} from FHIR server", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Create a new location in FHIR server
     */
    public Location createLocation(Location location) {
        try {
            MethodOutcome outcome = fhirClient.create()
                    .resource(location)
                    .execute();
            
            return (Location) outcome.getResource();
        } catch (Exception e) {
            logger.error("Error creating location in FHIR server", e);
            throw new RuntimeException("Failed to create location in FHIR server", e);
        }
    }
    
    /**
     * Update a location in FHIR server
     */
    public Location updateLocation(Location location) {
        try {
            MethodOutcome outcome = fhirClient.update()
                    .resource(location)
                    .execute();
            
            return (Location) outcome.getResource();
        } catch (Exception e) {
            logger.error("Error updating location in FHIR server", e);
            throw new RuntimeException("Failed to update location in FHIR server", e);
        }
    }
    
    /**
     * Delete a location from FHIR server
     */
    public void deleteLocation(String id) {
        try {
            fhirClient.delete()
                    .resourceById("Location", id)
                    .execute();
            
            logger.info("Deleted location with ID {} from FHIR server", id);
        } catch (Exception e) {
            logger.error("Error deleting location with ID {} from FHIR server", id, e);
            throw new RuntimeException("Failed to delete location from FHIR server", e);
        }
    }
    
    /**
     * Search locations by name
     */
    public List<Location> searchLocationsByName(String name) {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Location.class)
                    .where(Location.NAME.matches().value(name))
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Location> locations = new ArrayList<>();
            bundle.getEntry().forEach(entry -> {
                if (entry.getResource() instanceof Location) {
                    locations.add((Location) entry.getResource());
                }
            });
            
            return locations;
        } catch (Exception e) {
            logger.error("Error searching locations by name: {}", name, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get locations by organization
     */
    public List<Location> getLocationsByOrganization(String organizationId) {
        try {
            Bundle bundle = fhirClient.search()
                    .forResource(Location.class)
                    .where(Location.ORGANIZATION.hasId(organizationId))
                    .returnBundle(Bundle.class)
                    .execute();
            
            List<Location> locations = new ArrayList<>();
            bundle.getEntry().forEach(entry -> {
                if (entry.getResource() instanceof Location) {
                    locations.add((Location) entry.getResource());
                }
            });
            
            return locations;
        } catch (Exception e) {
            logger.error("Error fetching locations for organization: {}", organizationId, e);
            return new ArrayList<>();
        }
    }
}