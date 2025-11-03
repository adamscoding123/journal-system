package com.journalsystem.controller;

import com.journalsystem.model.Practitioner;
import com.journalsystem.service.PractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/practitioners")
@CrossOrigin(origins = "http://localhost:3000")
public class PractitionerController {

    @Autowired
    private PractitionerService practitionerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Practitioner>> getAllPractitioners() {
        return ResponseEntity.ok(practitionerService.getAllPractitioners());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Practitioner> getPractitionerById(@PathVariable Long id) {
        return ResponseEntity.ok(practitionerService.getPractitionerById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Practitioner> getPractitionerByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(practitionerService.getPractitionerByUserId(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Practitioner> updatePractitioner(@PathVariable Long id, @RequestBody Practitioner practitioner) {
        return ResponseEntity.ok(practitionerService.updatePractitioner(id, practitioner));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deletePractitioner(@PathVariable Long id) {
        practitionerService.deletePractitioner(id);
        return ResponseEntity.noContent().build();
    }
}
