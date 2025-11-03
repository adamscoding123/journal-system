package com.journalsystem.controller;

import com.journalsystem.model.Encounter;
import com.journalsystem.service.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/encounters")
@CrossOrigin(origins = "http://localhost:3000")
public class EncounterController {

    @Autowired
    private EncounterService encounterService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<List<Encounter>> getAllEncounters() {
        return ResponseEntity.ok(encounterService.getAllEncounters());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Encounter> getEncounterById(@PathVariable Long id) {
        return ResponseEntity.ok(encounterService.getEncounterById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Encounter>> getEncountersByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(encounterService.getEncountersByPatientId(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Encounter> createEncounter(@RequestBody Encounter encounter) {
        return ResponseEntity.ok(encounterService.createEncounter(encounter));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Encounter> updateEncounter(@PathVariable Long id, @RequestBody Encounter encounter) {
        return ResponseEntity.ok(encounterService.updateEncounter(id, encounter));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteEncounter(@PathVariable Long id) {
        encounterService.deleteEncounter(id);
        return ResponseEntity.noContent().build();
    }
}
