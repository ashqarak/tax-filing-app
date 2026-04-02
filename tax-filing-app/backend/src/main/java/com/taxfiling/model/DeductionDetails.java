package com.taxfiling.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deduction_details")
@Data
@NoArgsConstructor
public class DeductionDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 7)
    private String assessmentYear = "2025-26";

    // --- Chapter VI-A Deductions ---
    // Section 80C (max ₹1,50,000)
    private double sec80C_ppf = 0;
    private double sec80C_elss = 0;
    private double sec80C_lic = 0;
    private double sec80C_epf = 0;
    private double sec80C_nsc = 0;
    private double sec80C_tuitionFees = 0;
    private double sec80C_homeLoanPrincipal = 0;
    private double sec80C_sukanyaSamridhi = 0;
    private double sec80C_others = 0;

    // Section 80D – Health Insurance (max ₹25K self + ₹50K parents if senior)
    private double sec80D_selfFamily = 0;
    private double sec80D_parents = 0;
    private boolean parentsSeniorCitizen = false;

    // Section 80CCD(1B) – NPS (extra ₹50,000 over 80C)
    private double sec80CCD1B_nps = 0;

    // Section 80G – Donations
    private double sec80G_100percent = 0;   // 100% deduction donations
    private double sec80G_50percent = 0;    // 50% deduction donations

    // Section 80TTA/80TTB – Interest on savings/FD (senior)
    private double sec80TTA_savingsInterest = 0;  // max ₹10,000 for non-senior
    private double sec80TTB_fdInterest = 0;        // max ₹50,000 for senior 60+

    // Section 80E – Education Loan Interest
    private double sec80E_educationLoan = 0;

    // Section 80EEA – Home Loan (first-time buyers, max ₹1.5L)
    private double sec80EEA_homeLoan = 0;

    // Section 24(b) – Home Loan Interest (already in IncomeDetails as HOuse Prop)
    // But if self-occupied, it directly reduces income there

    // Additional deductions
    private double professionalTax = 0;  // max ₹2,500 per year
}
