package com.journalsystem.service;

import com.journalsystem.model.Observation;
import com.journalsystem.model.Patient;
import com.journalsystem.repository.ObservationRepository;
import com.journalsystem.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObservationService {

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private PatientRepository patientRepository;

    public List<Observation> getAllObservations() {
        return observationRepository.findAll();
    }

    public Observation getObservationById(Long id) {
        return observationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observation not found"));
    }

    public List<Observation> getObservationsByPatientId(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return observationRepository.findByPatientOrderByObservationDateDesc(patient);
    }

    public Observation createObservation(Observation observation) {
        return observationRepository.save(observation);
    }

    public Observation updateObservation(Long id, Observation observationDetails) {
        Observation observation = getObservationById(id);

        if (observationDetails.getObservationType() != null) {
            observation.setObservationType(observationDetails.getObservationType());
        }
        if (observationDetails.getValue() != null) {
            observation.setValue(observationDetails.getValue());
        }
        if (observationDetails.getUnit() != null) {
            observation.setUnit(observationDetails.getUnit());
        }
        if (observationDetails.getNotes() != null) {
            observation.setNotes(observationDetails.getNotes());
        }

        return observationRepository.save(observation);
    }

    public void deleteObservation(Long id) {
        observationRepository.deleteById(id);
    }
}
