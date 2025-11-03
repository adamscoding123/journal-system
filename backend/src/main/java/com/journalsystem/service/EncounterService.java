package com.journalsystem.service;

import com.journalsystem.model.Encounter;
import com.journalsystem.model.Patient;
import com.journalsystem.model.Practitioner;
import com.journalsystem.repository.EncounterRepository;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.repository.PractitionerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncounterService {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PractitionerRepository practitionerRepository;

    public List<Encounter> getAllEncounters() {
        return encounterRepository.findAll();
    }

    public Encounter getEncounterById(Long id) {
        return encounterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encounter not found"));
    }

    public List<Encounter> getEncountersByPatientId(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return encounterRepository.findByPatientOrderByEncounterDateDesc(patient);
    }

    public Encounter createEncounter(Encounter encounter) {
        return encounterRepository.save(encounter);
    }

    public Encounter updateEncounter(Long id, Encounter encounterDetails) {
        Encounter encounter = getEncounterById(id);

        if (encounterDetails.getEncounterType() != null) {
            encounter.setEncounterType(encounterDetails.getEncounterType());
        }
        if (encounterDetails.getStatus() != null) {
            encounter.setStatus(encounterDetails.getStatus());
        }
        if (encounterDetails.getNotes() != null) {
            encounter.setNotes(encounterDetails.getNotes());
        }
        if (encounterDetails.getReasonForVisit() != null) {
            encounter.setReasonForVisit(encounterDetails.getReasonForVisit());
        }

        return encounterRepository.save(encounter);
    }

    public void deleteEncounter(Long id) {
        encounterRepository.deleteById(id);
    }
}
