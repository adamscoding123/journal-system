package com.journalsystem.controller;

import com.journalsystem.model.Observation;
import com.journalsystem.service.ObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/observations")
@CrossOrigin(origins = "http://localhost:3000")
public class ObservationController {

    @Autowired
    private ObservationService observationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<List<Observation>> getAllObservations() {
        return ResponseEntity.ok(observationService.getAllObservations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Observation> getObservationById(@PathVariable Long id) {
        return ResponseEntity.ok(observationService.getObservationById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Observation>> getObservationsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(observationService.getObservationsByPatientId(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Observation> createObservation(@RequestBody Observation observation) {
        return ResponseEntity.ok(observationService.createObservation(observation));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Observation> updateObservation(@PathVariable Long id, @RequestBody Observation observation) {
        return ResponseEntity.ok(observationService.updateObservation(id, observation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteObservation(@PathVariable Long id) {
        observationService.deleteObservation(id);
        return ResponseEntity.noContent().build();
    }
}
