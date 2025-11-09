package com.journalsystem.service;

import com.journalsystem.model.Location;
import com.journalsystem.model.Organization;
import com.journalsystem.repository.LocationRepository;
import com.journalsystem.repository.OrganizationRepository;
import com.journalsystem.service.fhir.LocationFhirService;
import com.journalsystem.converter.LocationFhirConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private LocationFhirService locationFhirService;
    
    @Autowired
    private LocationFhirConverter locationFhirConverter;
    
    @Value("${fhir.enabled:false}")
    private boolean fhirEnabled;

    public List<Location> getAllLocations() {
        if (fhirEnabled) {
            // Get locations from FHIR server
            List<org.hl7.fhir.r4.model.Location> fhirLocations = locationFhirService.getAllLocations();
            return fhirLocations.stream()
                    .map(locationFhirConverter::fromFhir)
                    .collect(Collectors.toList());
        }
        return locationRepository.findAll();
    }

    public Location getLocationById(Long id) {
        if (fhirEnabled) {
            // Try FHIR first
            return locationFhirService.getLocationById(id.toString())
                    .map(locationFhirConverter::fromFhir)
                    .orElseGet(() -> locationRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Location not found")));
        }
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
    }

    public List<Location> getLocationsByOrganizationId(Long organizationId) {
        if (fhirEnabled) {
            // Get locations from FHIR server by organization
            List<org.hl7.fhir.r4.model.Location> fhirLocations = 
                    locationFhirService.getLocationsByOrganization(organizationId.toString());
            return fhirLocations.stream()
                    .map(fhirLocation -> {
                        Location location = locationFhirConverter.fromFhir(fhirLocation);
                        // Set dummy organization reference
                        Organization org = new Organization();
                        org.setId(organizationId);
                        location.setOrganization(org);
                        return location;
                    })
                    .collect(Collectors.toList());
        }
        
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return locationRepository.findByOrganization(organization);
    }

    public Location createLocation(Location location) {
        // Save locally first
        Location savedLocation = locationRepository.save(location);
        
        // If FHIR is enabled, also create in FHIR server
        if (fhirEnabled) {
            org.hl7.fhir.r4.model.Location fhirLocation = locationFhirConverter.toFhir(savedLocation);
            locationFhirService.createLocation(fhirLocation);
        }
        
        return savedLocation;
    }

    public Location updateLocation(Long id, Location locationDetails) {
        Location location = getLocationById(id);

        if (locationDetails.getName() != null) {
            location.setName(locationDetails.getName());
        }
        if (locationDetails.getAddress() != null) {
            location.setAddress(locationDetails.getAddress());
        }
        if (locationDetails.getCity() != null) {
            location.setCity(locationDetails.getCity());
        }
        if (locationDetails.getPostalCode() != null) {
            location.setPostalCode(locationDetails.getPostalCode());
        }
        if (locationDetails.getCountry() != null) {
            location.setCountry(locationDetails.getCountry());
        }
        if (locationDetails.getType() != null) {
            location.setType(locationDetails.getType());
        }

        // Save locally first
        Location savedLocation = locationRepository.save(location);
        
        // If FHIR is enabled, also update in FHIR server
        if (fhirEnabled) {
            org.hl7.fhir.r4.model.Location fhirLocation = locationFhirConverter.toFhir(savedLocation);
            locationFhirService.updateLocation(fhirLocation);
        }
        
        return savedLocation;
    }

    public void deleteLocation(Long id) {
        // Delete from FHIR if enabled
        if (fhirEnabled) {
            locationFhirService.deleteLocation(id.toString());
        }
        // Always delete locally
        locationRepository.deleteById(id);
    }
}