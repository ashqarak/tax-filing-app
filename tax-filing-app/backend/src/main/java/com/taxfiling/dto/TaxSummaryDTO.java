package com.taxfiling.dto;

import lombok.Data;

@Data
public class TaxSummaryDTO {

    private String assessmentYear;
    private String clientName;
    private String pan;

    // Income
    private double grossTotalIncome;
    private double salaryIncome;
    private double housePropertyIncome;
    private double capitalGainsIncome;
    private double otherSourcesIncome;

    // Old Regime
    private double totalDeductionsOld;
    private double taxableIncomeOld;
    private double baseTaxOld;
    private double surchargeOld;
    private double cessOld;
    private double totalTaxOld;
    private double rebate87AOld;

    // New Regime
    private double taxableIncomeNew;
    private double baseTaxNew;
    private double surchargeNew;
    private double cessNew;
    private double totalTaxNew;
    private double rebate87ANew;

    // Credits
    private double tdsCredits;
    private double advanceTaxCredits;

    // Final
    private String recommendedRegime;
    private double taxSavings;
    private double netTaxPayableOld;
    private double netTaxPayableNew;
    private double refundOld;
    private double refundNew;

    // HRA Breakdown
    private double hraExemption;

    // Deduction Breakdown
    private double sec80C_total;
    private double sec80D_total;
    private double sec80CCD1B;
    private double sec80G_total;
    private double sec80TTA_80TTB;
    private double sec80E;
    private double professionalTax;
    private double standardDeduction;
    private double homeLoanInterest;
}
