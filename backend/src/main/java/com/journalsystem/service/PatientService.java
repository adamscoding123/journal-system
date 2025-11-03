package com.journalsystem.service;

import com.journalsystem.model.Patient;
import com.journalsystem.model.User;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user"));
    }

    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);

        if (patientDetails.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientDetails.getDateOfBirth());
        }
        if (patientDetails.getAddress() != null) {
            patient.setAddress(patientDetails.getAddress());
        }
        if (patientDetails.getPhoneNumber() != null) {
            patient.setPhoneNumber(patientDetails.getPhoneNumber());
        }
        if (patientDetails.getBloodType() != null) {
            patient.setBloodType(patientDetails.getBloodType());
        }
        if (patientDetails.getAllergies() != null) {
            patient.setAllergies(patientDetails.getAllergies());
        }
        if (patientDetails.getMedications() != null) {
            patient.setMedications(patientDetails.getMedications());
        }

        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
