package com.journalsystem.controller;

import com.journalsystem.model.Condition;
import com.journalsystem.service.ConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conditions")
@CrossOrigin(origins = "http://localhost:3000")
public class ConditionController {

    @Autowired
    private ConditionService conditionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<List<Condition>> getAllConditions() {
        return ResponseEntity.ok(conditionService.getAllConditions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<Condition> getConditionById(@PathVariable Long id) {
        return ResponseEntity.ok(conditionService.getConditionById(id));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF', 'PATIENT')")
    public ResponseEntity<List<Condition>> getConditionsByPatientId(@PathVariable Long patientId) {
        if (patientId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(conditionService.getConditionsByPatientId(patientId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Condition> createCondition(@RequestBody Condition condition) {
        return ResponseEntity.ok(conditionService.createCondition(condition));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'STAFF')")
    public ResponseEntity<Condition> updateCondition(@PathVariable Long id, @RequestBody Condition condition) {
        return ResponseEntity.ok(conditionService.updateCondition(id, condition));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteCondition(@PathVariable Long id) {
        conditionService.deleteCondition(id);
        return ResponseEntity.noContent().build();
    }
}
