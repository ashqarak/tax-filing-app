package com.taxfiling.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_returns")
@Data
@NoArgsConstructor
public class TaxReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 7)
    private String assessmentYear = "2025-26";

    // Computed values
    private double grossTotalIncome = 0;
    private double totalDeductions = 0;
    private double taxableIncomeOldRegime = 0;
    private double taxableIncomeNewRegime = 0;

    private double taxOldRegime = 0;
    private double taxNewRegime = 0;
    private double surchargeOld = 0;
    private double surchargeNew = 0;
    private double cessOld = 0;
    private double cessNew = 0;
    private double totalTaxOld = 0;
    private double totalTaxNew = 0;

    private double tdsCredits = 0;
    private double advanceTaxCredits = 0;
    private double netTaxPayable = 0;
    private double refundAmount = 0;

    private String recommendedRegime = "NEW"; // OLD or NEW
    private double taxSavings = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime submittedAt;

    public enum Status {
        DRAFT, UNDER_REVIEW, FILED, REJECTED
    }
}
