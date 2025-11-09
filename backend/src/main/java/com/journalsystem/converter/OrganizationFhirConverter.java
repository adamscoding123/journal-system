package com.journalsystem.converter;

import com.journalsystem.model.Organization;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

/**
 * Converter between local Organization entity and FHIR Organization resource
 */
@Component
public class OrganizationFhirConverter {
    
    /**
     * Convert local Organization entity to FHIR Organization resource
     */
    public org.hl7.fhir.r4.model.Organization toFhir(Organization localOrg) {
        if (localOrg == null) {
            return null;
        }
        
        org.hl7.fhir.r4.model.Organization fhirOrg = new org.hl7.fhir.r4.model.Organization();
        
        // Set ID
        if (localOrg.getId() != null) {
            fhirOrg.setId(localOrg.getId().toString());
        }
        
        // Set name
        fhirOrg.setName(localOrg.getName());
        
        // Set type
        if (localOrg.getType() != null) {
            CodeableConcept type = new CodeableConcept();
            type.setText(localOrg.getType());
            fhirOrg.addType(type);
        }
        
        // Set address
        if (localOrg.getAddress() != null) {
            Address address = new Address();
            address.setUse(Address.AddressUse.WORK);
            address.setText(localOrg.getAddress());
            fhirOrg.addAddress(address);
        }
        
        // Set phone number
        if (localOrg.getPhoneNumber() != null) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(localOrg.getPhoneNumber());
            phone.setUse(ContactPoint.ContactPointUse.WORK);
            fhirOrg.addTelecom(phone);
        }
        
        // Set email
        if (localOrg.getEmail() != null) {
            ContactPoint email = new ContactPoint();
            email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            email.setValue(localOrg.getEmail());
            email.setUse(ContactPoint.ContactPointUse.WORK);
            fhirOrg.addTelecom(email);
        }
        
        // Set active status
        fhirOrg.setActive(true);
        
        return fhirOrg;
    }
    
    /**
     * Convert FHIR Organization resource to local Organization entity
     */
    public Organization fromFhir(org.hl7.fhir.r4.model.Organization fhirOrg) {
        if (fhirOrg == null) {
            return null;
        }
        
        Organization localOrg = new Organization();
        
        // Set ID
        if (fhirOrg.hasIdElement() && fhirOrg.getIdElement().hasIdPart()) {
            String fhirId = fhirOrg.getIdElement().getIdPart();
            try {
                localOrg.setId(Long.parseLong(fhirId));
            } catch (NumberFormatException e) {
                // For non-numeric FHIR IDs, generate a consistent ID based on hash
                localOrg.setId(Math.abs((long) fhirId.hashCode()));
            }
        }
        
        // Set name
        if (fhirOrg.hasName()) {
            localOrg.setName(fhirOrg.getName());
        }
        
        // Set type from first type
        if (fhirOrg.hasType() && !fhirOrg.getType().isEmpty()) {
            CodeableConcept firstType = fhirOrg.getType().get(0);
            if (firstType.hasText()) {
                localOrg.setType(firstType.getText());
            } else if (firstType.hasCoding() && !firstType.getCoding().isEmpty()) {
                localOrg.setType(firstType.getCoding().get(0).getDisplay());
            }
        }
        
        // Set address from first address
        if (fhirOrg.hasAddress() && !fhirOrg.getAddress().isEmpty()) {
            Address firstAddress = fhirOrg.getAddress().get(0);
            if (firstAddress.hasText()) {
                localOrg.setAddress(firstAddress.getText());
            } else if (firstAddress.hasLine()) {
                StringBuilder addressBuilder = new StringBuilder();
                firstAddress.getLine().forEach(line -> addressBuilder.append(line).append(" "));
                if (firstAddress.hasCity()) {
                    addressBuilder.append(", ").append(firstAddress.getCity());
                }
                if (firstAddress.hasPostalCode()) {
                    addressBuilder.append(" ").append(firstAddress.getPostalCode());
                }
                localOrg.setAddress(addressBuilder.toString().trim());
            }
        }
        
        // Set phone and email from telecom
        for (ContactPoint telecom : fhirOrg.getTelecom()) {
            if (telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                localOrg.setPhoneNumber(telecom.getValue());
            } else if (telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                localOrg.setEmail(telecom.getValue());
            }
        }
        
        return localOrg;
    }
}