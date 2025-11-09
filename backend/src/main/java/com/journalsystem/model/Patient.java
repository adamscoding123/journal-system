package com.journalsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(unique = true, nullable = false)
    private String personalNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column
    private String address;

    @Column
    private String phoneNumber;

    @Column
    private String bloodType;

    @Column(length = 2000)
    private String allergies;

    @Column(length = 2000)
    private String medications;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Transient fields for FHIR patients who don't have associated User accounts
    @Transient
    private String firstName;
    
    @Transient
    private String lastName;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
