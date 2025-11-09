package com.journalsystem.converter;

import com.journalsystem.model.Location;
import com.journalsystem.model.Organization;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

/**
 * Converter between local Location entity and FHIR Location resource
 */
@Component
public class LocationFhirConverter {
    
    /**
     * Convert local Location entity to FHIR Location resource
     */
    public org.hl7.fhir.r4.model.Location toFhir(Location localLocation) {
        if (localLocation == null) {
            return null;
        }
        
        org.hl7.fhir.r4.model.Location fhirLocation = new org.hl7.fhir.r4.model.Location();
        
        // Set ID
        if (localLocation.getId() != null) {
            fhirLocation.setId(localLocation.getId().toString());
        }
        
        // Set name
        fhirLocation.setName(localLocation.getName());
        
        // Set address
        Address address = new Address();
        address.setUse(Address.AddressUse.WORK);
        
        if (localLocation.getAddress() != null) {
            address.addLine(localLocation.getAddress());
        }
        if (localLocation.getCity() != null) {
            address.setCity(localLocation.getCity());
        }
        if (localLocation.getPostalCode() != null) {
            address.setPostalCode(localLocation.getPostalCode());
        }
        if (localLocation.getCountry() != null) {
            address.setCountry(localLocation.getCountry());
        }
        
        fhirLocation.setAddress(address);
        
        // Set type
        if (localLocation.getType() != null) {
            CodeableConcept type = new CodeableConcept();
            type.setText(localLocation.getType());
            fhirLocation.addType(type);
        }
        
        // Set managing organization reference
        if (localLocation.getOrganization() != null && localLocation.getOrganization().getId() != null) {
            Reference orgRef = new Reference();
            orgRef.setReference("Organization/" + localLocation.getOrganization().getId());
            if (localLocation.getOrganization().getName() != null) {
                orgRef.setDisplay(localLocation.getOrganization().getName());
            }
            fhirLocation.setManagingOrganization(orgRef);
        }
        
        // Set status
        fhirLocation.setStatus(org.hl7.fhir.r4.model.Location.LocationStatus.ACTIVE);
        
        // Set mode
        fhirLocation.setMode(org.hl7.fhir.r4.model.Location.LocationMode.INSTANCE);
        
        return fhirLocation;
    }
    
    /**
     * Convert FHIR Location resource to local Location entity
     */
    public Location fromFhir(org.hl7.fhir.r4.model.Location fhirLocation) {
        if (fhirLocation == null) {
            return null;
        }
        
        Location localLocation = new Location();
        
        // Set ID
        if (fhirLocation.hasIdElement() && fhirLocation.getIdElement().hasIdPart()) {
            String fhirId = fhirLocation.getIdElement().getIdPart();
            try {
                localLocation.setId(Long.parseLong(fhirId));
            } catch (NumberFormatException e) {
                // For non-numeric FHIR IDs, generate a consistent ID based on hash
                localLocation.setId(Math.abs((long) fhirId.hashCode()));
            }
        }
        
        // Set name
        if (fhirLocation.hasName()) {
            localLocation.setName(fhirLocation.getName());
        }
        
        // Set address
        if (fhirLocation.hasAddress()) {
            Address fhirAddress = fhirLocation.getAddress();
            
            if (fhirAddress.hasLine() && !fhirAddress.getLine().isEmpty()) {
                localLocation.setAddress(String.join(" ", 
                    fhirAddress.getLine().stream()
                        .map(StringType::getValue)
                        .toArray(String[]::new)));
            }
            
            if (fhirAddress.hasCity()) {
                localLocation.setCity(fhirAddress.getCity());
            }
            
            if (fhirAddress.hasPostalCode()) {
                localLocation.setPostalCode(fhirAddress.getPostalCode());
            }
            
            if (fhirAddress.hasCountry()) {
                localLocation.setCountry(fhirAddress.getCountry());
            }
        }
        
        // Set type from first type
        if (fhirLocation.hasType() && !fhirLocation.getType().isEmpty()) {
            CodeableConcept firstType = fhirLocation.getType().get(0);
            if (firstType.hasText()) {
                localLocation.setType(firstType.getText());
            } else if (firstType.hasCoding() && !firstType.getCoding().isEmpty()) {
                localLocation.setType(firstType.getCoding().get(0).getDisplay());
            }
        }
        
        // Note: Organization reference needs to be handled separately in the service layer
        // as it requires fetching the actual Organization entity
        
        return localLocation;
    }
}