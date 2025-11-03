package com.journalsystem.service;

import com.journalsystem.dto.LoginRequest;
import com.journalsystem.dto.LoginResponse;
import com.journalsystem.dto.RegisterRequest;
import com.journalsystem.model.Patient;
import com.journalsystem.model.Practitioner;
import com.journalsystem.model.Role;
import com.journalsystem.model.User;
import com.journalsystem.repository.PatientRepository;
import com.journalsystem.repository.PractitionerRepository;
import com.journalsystem.repository.UserRepository;
import com.journalsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PractitionerRepository practitionerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());

        user = userRepository.save(user);

        // Create Patient or Practitioner based on role
        if (request.getRole() == Role.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setPersonalNumber(request.getPersonalNumber());
            patient.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            patient.setAddress(request.getAddress());
            patient.setPhoneNumber(request.getPhoneNumber());
            patient.setBloodType(request.getBloodType());
            patient.setAllergies(request.getAllergies());
            patient.setMedications(request.getMedications());
            patientRepository.save(patient);
        } else if (request.getRole() == Role.DOCTOR || request.getRole() == Role.STAFF) {
            Practitioner practitioner = new Practitioner();
            practitioner.setUser(user);
            practitioner.setSpecialization(request.getSpecialization());
            practitioner.setLicenseNumber(request.getLicenseNumber());
            practitioner.setPhoneNumber(request.getPhoneNumber());
            practitionerRepository.save(practitioner);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token, user.getUsername(), user.getRole().name(), user.getId());
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token, user.getUsername(), user.getRole().name(), user.getId());
    }
}
