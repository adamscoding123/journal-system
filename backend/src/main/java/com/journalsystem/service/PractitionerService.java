package com.journalsystem.service;

import com.journalsystem.model.Practitioner;
import com.journalsystem.repository.PractitionerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PractitionerService {

    @Autowired
    private PractitionerRepository practitionerRepository;

    public List<Practitioner> getAllPractitioners() {
        return practitionerRepository.findAll();
    }

    public Practitioner getPractitionerById(Long id) {
        return practitionerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Practitioner not found"));
    }

    public Practitioner getPractitionerByUserId(Long userId) {
        return practitionerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Practitioner not found for user"));
    }

    public Practitioner updatePractitioner(Long id, Practitioner practitionerDetails) {
        Practitioner practitioner = getPractitionerById(id);

        if (practitionerDetails.getSpecialization() != null) {
            practitioner.setSpecialization(practitionerDetails.getSpecialization());
        }
        if (practitionerDetails.getPhoneNumber() != null) {
            practitioner.setPhoneNumber(practitionerDetails.getPhoneNumber());
        }
        if (practitionerDetails.getOrganization() != null) {
            practitioner.setOrganization(practitionerDetails.getOrganization());
        }

        return practitionerRepository.save(practitioner);
    }

    public void deletePractitioner(Long id) {
        practitionerRepository.deleteById(id);
    }
}
