package com.journalsystem.converter;

import com.journalsystem.model.Patient;
import com.journalsystem.model.User;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PatientFhirConverter {
    
    /**
     * Convert local Patient entity to FHIR Patient resource
     */
    public org.hl7.fhir.r4.model.Patient toFhir(Patient localPatient) {
        if (localPatient == null) {
            return null;
        }
        
        org.hl7.fhir.r4.model.Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();
        
        // Set ID
        if (localPatient.getId() != null) {
            fhirPatient.setId(localPatient.getId().toString());
        }
        
        // Set personal number as identifier (Swedish format)
        Identifier personalNumberIdentifier = new Identifier();
        personalNumberIdentifier.setSystem("http://electronichealth.se/identifier/personnummer");
        personalNumberIdentifier.setValue(localPatient.getPersonalNumber());
        fhirPatient.addIdentifier(personalNumberIdentifier);
        
        // Set name from User
        if (localPatient.getUser() != null) {
            HumanName name = new HumanName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.addGiven(localPatient.getUser().getFirstName());
            name.setFamily(localPatient.getUser().getLastName());
            fhirPatient.addName(name);
        }
        
        // Set birth date
        if (localPatient.getDateOfBirth() != null) {
            fhirPatient.setBirthDate(Date.from(localPatient.getDateOfBirth()
                    .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        }
        
        // Set address
        if (localPatient.getAddress() != null) {
            Address address = new Address();
            address.setUse(Address.AddressUse.HOME);
            address.setText(localPatient.getAddress());
            fhirPatient.addAddress(address);
        }
        
        // Set phone number
        if (localPatient.getPhoneNumber() != null) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(localPatient.getPhoneNumber());
            phone.setUse(ContactPoint.ContactPointUse.HOME);
            fhirPatient.addTelecom(phone);
        }
        
        // Set blood type as extension
        if (localPatient.getBloodType() != null) {
            Extension bloodTypeExt = new Extension();
            bloodTypeExt.setUrl("http://example.org/fhir/StructureDefinition/bloodType");
            bloodTypeExt.setValue(new StringType(localPatient.getBloodType()));
            fhirPatient.addExtension(bloodTypeExt);
        }
        
        // Set allergies as extension
        if (localPatient.getAllergies() != null) {
            Extension allergiesExt = new Extension();
            allergiesExt.setUrl("http://example.org/fhir/StructureDefinition/allergies");
            allergiesExt.setValue(new StringType(localPatient.getAllergies()));
            fhirPatient.addExtension(allergiesExt);
        }
        
        // Set medications as extension
        if (localPatient.getMedications() != null) {
            Extension medicationsExt = new Extension();
            medicationsExt.setUrl("http://example.org/fhir/StructureDefinition/medications");
            medicationsExt.setValue(new StringType(localPatient.getMedications()));
            fhirPatient.addExtension(medicationsExt);
        }
        
        return fhirPatient;
    }
    
    /**
     * Convert FHIR Patient resource to local Patient entity
     */
    public Patient fromFhir(org.hl7.fhir.r4.model.Patient fhirPatient) {
        if (fhirPatient == null) {
            return null;
        }
        
        Patient localPatient = new Patient();
        
        // Set ID - store the FHIR ID in a consistent way
        if (fhirPatient.hasIdElement() && fhirPatient.getIdElement().hasIdPart()) {
            String fhirId = fhirPatient.getIdElement().getIdPart();
            try {
                localPatient.setId(Long.parseLong(fhirId));
            } catch (NumberFormatException e) {
                // For non-numeric FHIR IDs, generate a consistent ID based on hash
                // Use Math.abs to ensure positive IDs
                localPatient.setId(Math.abs((long) fhirId.hashCode()));
            }
            
            // Store the original FHIR ID as an extension or in a transient field
            // This will help with lookups
        }
        
        // Get personal number from identifier
        for (Identifier identifier : fhirPatient.getIdentifier()) {
            if ("http://electronichealth.se/identifier/personnummer".equals(identifier.getSystem())) {
                localPatient.setPersonalNumber(identifier.getValue());
                break;
            }
        }
        
        // Set birth date
        if (fhirPatient.hasBirthDate()) {
            localPatient.setDateOfBirth(fhirPatient.getBirthDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        // Set address
        if (fhirPatient.hasAddress() && !fhirPatient.getAddress().isEmpty()) {
            Address firstAddress = fhirPatient.getAddress().get(0);
            localPatient.setAddress(firstAddress.getText() != null ? 
                    firstAddress.getText() : firstAddress.getLine().toString());
        }
        
        // Set phone number
        for (ContactPoint telecom : fhirPatient.getTelecom()) {
            if (telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                localPatient.setPhoneNumber(telecom.getValue());
                break;
            }
        }
        
        // Set name from FHIR patient
        if (fhirPatient.hasName() && !fhirPatient.getName().isEmpty()) {
            HumanName name = fhirPatient.getName().get(0);
            if (name.hasGiven() && !name.getGiven().isEmpty()) {
                localPatient.setFirstName(name.getGiven().get(0).getValue());
            }
            if (name.hasFamily()) {
                localPatient.setLastName(name.getFamily());
            }
        }
        
        // Get extensions
        for (Extension extension : fhirPatient.getExtension()) {
            String url = extension.getUrl();
            if ("http://example.org/fhir/StructureDefinition/bloodType".equals(url)) {
                localPatient.setBloodType(extension.getValue().toString());
            } else if ("http://example.org/fhir/StructureDefinition/allergies".equals(url)) {
                localPatient.setAllergies(extension.getValue().toString());
            } else if ("http://example.org/fhir/StructureDefinition/medications".equals(url)) {
                localPatient.setMedications(extension.getValue().toString());
            }
        }
        
        // Note: User association needs to be handled separately in the service layer
        
        return localPatient;
    }
}