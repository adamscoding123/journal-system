package com.journalsystem.converter;

import com.journalsystem.model.Observation;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;

@Component
public class ObservationFhirConverter {
    
    /**
     * Convert local Observation entity to FHIR Observation resource
     */
    public org.hl7.fhir.r4.model.Observation toFhir(Observation localObservation) {
        if (localObservation == null) {
            return null;
        }
        
        org.hl7.fhir.r4.model.Observation fhirObservation = new org.hl7.fhir.r4.model.Observation();
        
        // Set ID
        if (localObservation.getId() != null) {
            fhirObservation.setId(localObservation.getId().toString());
        }
        
        // Set status - all observations are final in our system
        fhirObservation.setStatus(org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL);
        
        // Set code based on observation type
        CodeableConcept code = new CodeableConcept();
        code.setText(localObservation.getObservationType());
        fhirObservation.setCode(code);
        
        // Set subject (patient)
        if (localObservation.getPatient() != null) {
            Reference patientRef = new Reference();
            patientRef.setReference("Patient/" + localObservation.getPatient().getId());
            fhirObservation.setSubject(patientRef);
        }
        
        // Set encounter reference
        if (localObservation.getEncounter() != null) {
            Reference encounterRef = new Reference();
            encounterRef.setReference("Encounter/" + localObservation.getEncounter().getId());
            fhirObservation.setEncounter(encounterRef);
        }
        
        // Set performer (practitioner)
        if (localObservation.getPractitioner() != null) {
            Reference practitionerRef = new Reference();
            practitionerRef.setReference("Practitioner/" + localObservation.getPractitioner().getId());
            fhirObservation.addPerformer(practitionerRef);
        }
        
        // Set value - try to parse as numeric first
        try {
            BigDecimal numericValue = new BigDecimal(localObservation.getValue());
            Quantity quantity = new Quantity();
            quantity.setValue(numericValue);
            if (localObservation.getUnit() != null) {
                quantity.setUnit(localObservation.getUnit());
            }
            fhirObservation.setValue(quantity);
        } catch (NumberFormatException e) {
            // If not numeric, use string value
            StringType stringValue = new StringType(localObservation.getValue());
            fhirObservation.setValue(stringValue);
        }
        
        // Set effective date
        if (localObservation.getObservationDate() != null) {
            DateTimeType effectiveDateTime = new DateTimeType();
            effectiveDateTime.setValue(Date.from(localObservation.getObservationDate()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            fhirObservation.setEffective(effectiveDateTime);
        }
        
        // Set notes
        if (localObservation.getNotes() != null) {
            Annotation annotation = new Annotation();
            annotation.setText(localObservation.getNotes());
            fhirObservation.addNote(annotation);
        }
        
        return fhirObservation;
    }
    
    /**
     * Convert FHIR Observation resource to local Observation entity
     */
    public Observation fromFhir(org.hl7.fhir.r4.model.Observation fhirObservation) {
        if (fhirObservation == null) {
            return null;
        }
        
        Observation localObservation = new Observation();
        
        // Set ID
        if (fhirObservation.hasIdElement() && fhirObservation.getIdElement().hasIdPart()) {
            try {
                localObservation.setId(Long.parseLong(fhirObservation.getIdElement().getIdPart()));
            } catch (NumberFormatException e) {
                // Handle non-numeric IDs appropriately
            }
        }
        
        // Set observation type from code
        if (fhirObservation.hasCode()) {
            localObservation.setObservationType(fhirObservation.getCode().getText());
        }
        
        // Set value
        if (fhirObservation.hasValue()) {
            Type value = fhirObservation.getValue();
            if (value instanceof Quantity) {
                Quantity quantity = (Quantity) value;
                localObservation.setValue(quantity.getValue().toString());
                localObservation.setUnit(quantity.getUnit());
            } else if (value instanceof StringType) {
                localObservation.setValue(((StringType) value).getValue());
            } else {
                localObservation.setValue(value.toString());
            }
        }
        
        // Set observation date
        if (fhirObservation.hasEffective()) {
            Type effective = fhirObservation.getEffective();
            if (effective instanceof DateTimeType) {
                DateTimeType dateTime = (DateTimeType) effective;
                localObservation.setObservationDate(dateTime.getValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        }
        
        // Set notes
        if (fhirObservation.hasNote() && !fhirObservation.getNote().isEmpty()) {
            StringBuilder notes = new StringBuilder();
            for (Annotation annotation : fhirObservation.getNote()) {
                notes.append(annotation.getText()).append("\n");
            }
            localObservation.setNotes(notes.toString().trim());
        }
        
        // Note: Patient, Encounter, and Practitioner associations need to be handled separately in the service layer
        
        return localObservation;
    }
}