package com.journalsystem.converter;

import com.journalsystem.model.Encounter;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;

@Component
public class EncounterFhirConverter {
    
    /**
     * Convert local Encounter entity to FHIR Encounter resource
     */
    public org.hl7.fhir.r4.model.Encounter toFhir(Encounter localEncounter) {
        if (localEncounter == null) {
            return null;
        }
        
        org.hl7.fhir.r4.model.Encounter fhirEncounter = new org.hl7.fhir.r4.model.Encounter();
        
        // Set ID
        if (localEncounter.getId() != null) {
            fhirEncounter.setId(localEncounter.getId().toString());
        }
        
        // Set status
        if (localEncounter.getStatus() != null) {
            switch (localEncounter.getStatus().toLowerCase()) {
                case "planned":
                    fhirEncounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.PLANNED);
                    break;
                case "in-progress":
                    fhirEncounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.INPROGRESS);
                    break;
                case "finished":
                    fhirEncounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED);
                    break;
                case "cancelled":
                    fhirEncounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.CANCELLED);
                    break;
                default:
                    fhirEncounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.UNKNOWN);
            }
        } else {
            fhirEncounter.setStatus(org.hl7.fhir.r4.model.Encounter.EncounterStatus.UNKNOWN);
        }
        
        // Set class (type of encounter)
        Coding classCoding = new Coding();
        if (localEncounter.getEncounterType() != null) {
            switch (localEncounter.getEncounterType().toLowerCase()) {
                case "inpatient":
                    classCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                            .setCode("IMP")
                            .setDisplay("inpatient encounter");
                    break;
                case "outpatient":
                    classCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                            .setCode("AMB")
                            .setDisplay("ambulatory");
                    break;
                case "emergency":
                    classCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                            .setCode("EMER")
                            .setDisplay("emergency");
                    break;
                default:
                    classCoding.setDisplay(localEncounter.getEncounterType());
            }
        }
        fhirEncounter.setClass_(classCoding);
        
        // Set subject (patient)
        if (localEncounter.getPatient() != null) {
            Reference patientRef = new Reference();
            patientRef.setReference("Patient/" + localEncounter.getPatient().getId());
            fhirEncounter.setSubject(patientRef);
        }
        
        // Set participant (practitioner)
        if (localEncounter.getPractitioner() != null) {
            org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent participant = 
                new org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent();
            
            Reference practitionerRef = new Reference();
            practitionerRef.setReference("Practitioner/" + localEncounter.getPractitioner().getId());
            participant.setIndividual(practitionerRef);
            
            // Set participant type as primary performer
            CodeableConcept participantType = new CodeableConcept();
            participantType.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType")
                .setCode("PPRF")
                .setDisplay("primary performer");
            participant.addType(participantType);
            
            fhirEncounter.addParticipant(participant);
        }
        
        // Set period
        if (localEncounter.getEncounterDate() != null) {
            Period period = new Period();
            Date encounterDate = Date.from(localEncounter.getEncounterDate()
                    .atZone(ZoneId.systemDefault()).toInstant());
            period.setStart(encounterDate);
            // Assume the encounter is finished on the same day if status is finished
            if ("finished".equalsIgnoreCase(localEncounter.getStatus())) {
                period.setEnd(encounterDate);
            }
            fhirEncounter.setPeriod(period);
        }
        
        // Set location
        if (localEncounter.getLocation() != null) {
            org.hl7.fhir.r4.model.Encounter.EncounterLocationComponent locationComponent = 
                new org.hl7.fhir.r4.model.Encounter.EncounterLocationComponent();
            Reference locationRef = new Reference();
            locationRef.setReference("Location/" + localEncounter.getLocation().getId());
            locationComponent.setLocation(locationRef);
            fhirEncounter.addLocation(locationComponent);
        }
        
        // Set reason for visit
        if (localEncounter.getReasonForVisit() != null) {
            CodeableConcept reason = new CodeableConcept();
            reason.setText(localEncounter.getReasonForVisit());
            fhirEncounter.addReasonCode(reason);
        }
        
        // Set notes as text
        if (localEncounter.getNotes() != null) {
            Narrative text = new Narrative();
            text.setStatus(Narrative.NarrativeStatus.ADDITIONAL);
            text.setDivAsString("<div>" + localEncounter.getNotes() + "</div>");
            fhirEncounter.setText(text);
        }
        
        return fhirEncounter;
    }
    
    /**
     * Convert FHIR Encounter resource to local Encounter entity
     */
    public Encounter fromFhir(org.hl7.fhir.r4.model.Encounter fhirEncounter) {
        if (fhirEncounter == null) {
            return null;
        }
        
        Encounter localEncounter = new Encounter();
        
        // Set ID
        if (fhirEncounter.hasIdElement() && fhirEncounter.getIdElement().hasIdPart()) {
            try {
                localEncounter.setId(Long.parseLong(fhirEncounter.getIdElement().getIdPart()));
            } catch (NumberFormatException e) {
                // Handle non-numeric IDs appropriately
            }
        }
        
        // Set status
        if (fhirEncounter.hasStatus()) {
            localEncounter.setStatus(fhirEncounter.getStatus().toCode());
        }
        
        // Set encounter type from class
        if (fhirEncounter.hasClass_()) {
            localEncounter.setEncounterType(fhirEncounter.getClass_().getDisplay());
        }
        
        // Set encounter date from period
        if (fhirEncounter.hasPeriod()) {
            Period period = fhirEncounter.getPeriod();
            if (period.hasStart()) {
                localEncounter.setEncounterDate(period.getStart().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        }
        
        // Set reason for visit
        if (fhirEncounter.hasReasonCode() && !fhirEncounter.getReasonCode().isEmpty()) {
            localEncounter.setReasonForVisit(fhirEncounter.getReasonCode().get(0).getText());
        }
        
        // Set notes from text
        if (fhirEncounter.hasText() && fhirEncounter.getText().hasDiv()) {
            String divContent = fhirEncounter.getText().getDivAsString();
            // Remove HTML tags
            localEncounter.setNotes(divContent.replaceAll("<[^>]*>", ""));
        }
        
        // Note: Patient, Practitioner, and Location associations need to be handled separately in the service layer
        
        return localEncounter;
    }
}