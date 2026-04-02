package com.taxfiling.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_profiles")
@Data
@NoArgsConstructor
public class ClientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 10)
    private String pan;

    @Column(length = 16)
    private String aadhaar;

    private String dateOfBirth;   // format: YYYY-MM-DD
    private String mobile;
    private String address;
    private String city;
    private String state;
    private String pincode;

    @Enumerated(EnumType.STRING)
    private ResidentialStatus residentialStatus = ResidentialStatus.RESIDENT;

    @Enumerated(EnumType.STRING)
    private AgeCategory ageCategory = AgeCategory.BELOW_60;

    public enum ResidentialStatus {
        RESIDENT, NON_RESIDENT, NOT_ORDINARILY_RESIDENT
    }

    public enum AgeCategory {
        BELOW_60,       // Standard
        SENIOR_60_80,   // 60–79 years
        SUPER_SENIOR    // 80+ years
    }
}
