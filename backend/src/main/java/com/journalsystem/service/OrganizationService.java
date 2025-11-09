package com.journalsystem.service;

import com.journalsystem.model.Organization;
import com.journalsystem.repository.OrganizationRepository;
import com.journalsystem.service.fhir.OrganizationFhirService;
import com.journalsystem.converter.OrganizationFhirConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private OrganizationFhirService organizationFhirService;
    
    @Autowired
    private OrganizationFhirConverter organizationFhirConverter;
    
    @Value("${fhir.enabled:false}")
    private boolean fhirEnabled;

    public List<Organization> getAllOrganizations() {
        if (fhirEnabled) {
            // Get organizations from FHIR server
            List<org.hl7.fhir.r4.model.Organization> fhirOrgs = organizationFhirService.getAllOrganizations();
            return fhirOrgs.stream()
                    .map(organizationFhirConverter::fromFhir)
                    .collect(Collectors.toList());
        }
        return organizationRepository.findAll();
    }

    public Organization getOrganizationById(Long id) {
        if (fhirEnabled) {
            // Try FHIR first
            return organizationFhirService.getOrganizationById(id.toString())
                    .map(organizationFhirConverter::fromFhir)
                    .orElseGet(() -> organizationRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Organization not found")));
        }
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    public Organization createOrganization(Organization organization) {
        // Save locally first
        Organization savedOrg = organizationRepository.save(organization);
        
        // If FHIR is enabled, also create in FHIR server
        if (fhirEnabled) {
            org.hl7.fhir.r4.model.Organization fhirOrg = organizationFhirConverter.toFhir(savedOrg);
            organizationFhirService.createOrganization(fhirOrg);
        }
        
        return savedOrg;
    }

    public Organization updateOrganization(Long id, Organization organizationDetails) {
        Organization organization = getOrganizationById(id);

        if (organizationDetails.getName() != null) {
            organization.setName(organizationDetails.getName());
        }
        if (organizationDetails.getAddress() != null) {
            organization.setAddress(organizationDetails.getAddress());
        }
        if (organizationDetails.getPhoneNumber() != null) {
            organization.setPhoneNumber(organizationDetails.getPhoneNumber());
        }
        if (organizationDetails.getEmail() != null) {
            organization.setEmail(organizationDetails.getEmail());
        }
        if (organizationDetails.getType() != null) {
            organization.setType(organizationDetails.getType());
        }

        // Save locally first
        Organization savedOrg = organizationRepository.save(organization);
        
        // If FHIR is enabled, also update in FHIR server
        if (fhirEnabled) {
            org.hl7.fhir.r4.model.Organization fhirOrg = organizationFhirConverter.toFhir(savedOrg);
            organizationFhirService.updateOrganization(fhirOrg);
        }
        
        return savedOrg;
    }

    public void deleteOrganization(Long id) {
        // Delete from FHIR if enabled
        if (fhirEnabled) {
            organizationFhirService.deleteOrganization(id.toString());
        }
        // Always delete locally
        organizationRepository.deleteById(id);
    }
}