package com.journalsystem.converter;

import com.journalsystem.model.Condition;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;

@Component
public class ConditionFhirConverter {
    
    /**
     * Convert local Condition entity to FHIR Condition resource
     */
    public org.hl7.fhir.r4.model.Condition toFhir(Condition localCondition) {
        if (localCondition == null) {
            return null;
        }
        
        org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
        
        // Set ID
        if (localCondition.getId() != null) {
            fhirCondition.setId(localCondition.getId().toString());
        }
        
        // Set clinical status
        if (localCondition.getStatus() != null) {
            CodeableConcept clinicalStatus = new CodeableConcept();
            if ("active".equalsIgnoreCase(localCondition.getStatus())) {
                clinicalStatus.addCoding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                    .setCode("active");
            } else if ("resolved".equalsIgnoreCase(localCondition.getStatus())) {
                clinicalStatus.addCoding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                    .setCode("resolved");
            } else {
                clinicalStatus.setText(localCondition.getStatus());
            }
            fhirCondition.setClinicalStatus(clinicalStatus);
        }
        
        // Set verification status - assume all conditions are confirmed
        CodeableConcept verificationStatus = new CodeableConcept();
        verificationStatus.addCoding()
            .setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
            .setCode("confirmed");
        fhirCondition.setVerificationStatus(verificationStatus);
        
        // Set code (diagnosis)
        CodeableConcept code = new CodeableConcept();
        code.setText(localCondition.getDiagnosis());
        if (localCondition.getCode() != null) {
            // Assume ICD-10 if code is provided
            Coding coding = new Coding();
            coding.setSystem("http://hl7.org/fhir/sid/icd-10");
            coding.setCode(localCondition.getCode());
            coding.setDisplay(localCondition.getDiagnosis());
            code.addCoding(coding);
        }
        fhirCondition.setCode(code);
        
        // Set subject (patient)
        if (localCondition.getPatient() != null) {
            Reference patientRef = new Reference();
            patientRef.setReference("Patient/" + localCondition.getPatient().getId());
            fhirCondition.setSubject(patientRef);
        }
        
        // Set encounter reference
        if (localCondition.getEncounter() != null) {
            Reference encounterRef = new Reference();
            encounterRef.setReference("Encounter/" + localCondition.getEncounter().getId());
            fhirCondition.setEncounter(encounterRef);
        }
        
        // Set recorder (practitioner)
        if (localCondition.getPractitioner() != null) {
            Reference practitionerRef = new Reference();
            practitionerRef.setReference("Practitioner/" + localCondition.getPractitioner().getId());
            fhirCondition.setRecorder(practitionerRef);
        }
        
        // Set severity
        if (localCondition.getSeverity() != null) {
            CodeableConcept severity = new CodeableConcept();
            severity.setText(localCondition.getSeverity());
            fhirCondition.setSeverity(severity);
        }
        
        // Set onset date
        if (localCondition.getDiagnosisDate() != null) {
            DateTimeType onsetDateTime = new DateTimeType();
            onsetDateTime.setValue(Date.from(localCondition.getDiagnosisDate()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            fhirCondition.setOnset(onsetDateTime);
        }
        
        // Set notes
        if (localCondition.getNotes() != null) {
            Annotation annotation = new Annotation();
            annotation.setText(localCondition.getNotes());
            fhirCondition.addNote(annotation);
        }
        
        return fhirCondition;
    }
    
    /**
     * Convert FHIR Condition resource to local Condition entity
     */
    public Condition fromFhir(org.hl7.fhir.r4.model.Condition fhirCondition) {
        if (fhirCondition == null) {
            return null;
        }
        
        Condition localCondition = new Condition();
        
        // Set ID
        if (fhirCondition.hasIdElement() && fhirCondition.getIdElement().hasIdPart()) {
            try {
                localCondition.setId(Long.parseLong(fhirCondition.getIdElement().getIdPart()));
            } catch (NumberFormatException e) {
                // Handle non-numeric IDs appropriately
            }
        }
        
        // Set diagnosis from code
        if (fhirCondition.hasCode()) {
            localCondition.setDiagnosis(fhirCondition.getCode().getText());
            if (!fhirCondition.getCode().getCoding().isEmpty()) {
                Coding firstCoding = fhirCondition.getCode().getCoding().get(0);
                localCondition.setCode(firstCoding.getCode());
            }
        }
        
        // Set status
        if (fhirCondition.hasClinicalStatus()) {
            if (!fhirCondition.getClinicalStatus().getCoding().isEmpty()) {
                localCondition.setStatus(fhirCondition.getClinicalStatus().getCoding().get(0).getCode());
            } else {
                localCondition.setStatus(fhirCondition.getClinicalStatus().getText());
            }
        }
        
        // Set severity
        if (fhirCondition.hasSeverity()) {
            localCondition.setSeverity(fhirCondition.getSeverity().getText());
        }
        
        // Set diagnosis date from onset
        if (fhirCondition.hasOnset()) {
            Type onset = fhirCondition.getOnset();
            if (onset instanceof DateTimeType) {
                DateTimeType dateTime = (DateTimeType) onset;
                localCondition.setDiagnosisDate(dateTime.getValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        }
        
        // Set notes
        if (fhirCondition.hasNote() && !fhirCondition.getNote().isEmpty()) {
            StringBuilder notes = new StringBuilder();
            for (Annotation annotation : fhirCondition.getNote()) {
                notes.append(annotation.getText()).append("\n");
            }
            localCondition.setNotes(notes.toString().trim());
        }
        
        // Note: Patient, Encounter, and Practitioner associations need to be handled separately in the service layer
        
        return localCondition;
    }
}