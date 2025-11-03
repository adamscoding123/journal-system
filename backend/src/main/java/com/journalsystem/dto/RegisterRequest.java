package com.journalsystem.dto;

import com.journalsystem.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    // Patient specific fields
    private String personalNumber;
    private String dateOfBirth;
    private String address;
    private String phoneNumber;

    // Practitioner specific fields
    private String specialization;
    private String licenseNumber;
}
