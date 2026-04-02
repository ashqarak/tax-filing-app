package com.taxfiling.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "income_details")
@Data
@NoArgsConstructor
public class IncomeDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 7)
    private String assessmentYear = "2025-26"; // e.g., "2025-26"

    // --- Salary Income ---
    private double basicSalary = 0;
    private double hraReceived = 0;
    private double ltaReceived = 0;
    private double specialAllowance = 0;
    private double otherAllowances = 0;
    private double bonus = 0;

    // Employer details for HRA
    private String cityType = "METRO"; // METRO or NON_METRO
    private double rentPaidMonthly = 0;

    // --- House Property ---
    private double annualRentalIncome = 0;
    private double municipalTaxPaid = 0;
    private double homeLoanInterestSelfOccupied = 0;

    // --- Capital Gains ---
    private double shortTermCapitalGains = 0;  // taxed at slab or 15%
    private double ltcgExemptLimit = 0;        // gains up to ₹1.25L are exempt
    private double longTermCapitalGains = 0;   // taxed at 12.5%
    private double stcgSpecialRate = 0;        // STCG under 111A @ 15%

    // --- Other Sources ---
    private double interestFromSavings = 0;
    private double interestFromFD = 0;
    private double dividendIncome = 0;
    private double otherIncome = 0;

    // --- TDS Already Deducted ---
    private double tdsSalary = 0;
    private double tdsOther = 0;
    private double advanceTaxPaid = 0;
    private double selfAssessmentTax = 0;
}
