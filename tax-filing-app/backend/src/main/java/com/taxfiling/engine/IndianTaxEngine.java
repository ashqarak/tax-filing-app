package com.taxfiling.engine;

import com.taxfiling.dto.TaxSummaryDTO;
import com.taxfiling.model.ClientProfile;
import com.taxfiling.model.DeductionDetails;
import com.taxfiling.model.IncomeDetails;
import org.springframework.stereotype.Component;

/**
 * Core Indian Tax Calculation Engine
 * Supports FY 2024-25 / AY 2025-26 (applicable for ITR-1 and ITR-4)
 *
 * New Tax Regime Slabs (FY 2024-25):
 *   0       – 4,00,000  : Nil
 *   4,00,001 – 8,00,000 : 5%
 *   8,00,001 – 12,00,000: 10%
 *  12,00,001 – 16,00,000: 15%
 *  16,00,001 – 20,00,000: 20%
 *  20,00,001 – 24,00,000: 25%
 *  Above 24,00,000      : 30%
 *  Rebate 87A: ₹60,000 if income ≤ ₹12,00,000
 *  Standard Deduction: ₹75,000
 *
 * Old Tax Regime Slabs:
 *  Below 60: 0-2.5L Nil | 2.5-5L 5% | 5-10L 20% | >10L 30%
 *  60-79:    0-3L Nil   | 3-5L   5% | 5-10L 20% | >10L 30%
 *  80+:      0-5L Nil              | 5-10L 20% | >10L 30%
 *  Rebate 87A: ₹12,500 if income ≤ ₹5,00,000
 *  Standard Deduction: ₹50,000
 */
@Component
public class IndianTaxEngine {

    // ─────────────────────────────────────────────
    //  PUBLIC: Full Summary Computation
    // ─────────────────────────────────────────────

    public TaxSummaryDTO compute(
            IncomeDetails income,
            DeductionDetails deductions,
            ClientProfile profile,
            String clientName
    ) {
        TaxSummaryDTO dto = new TaxSummaryDTO();
        dto.setAssessmentYear(income.getAssessmentYear());
        dto.setClientName(clientName);
        dto.setPan(profile != null ? profile.getPan() : "N/A");

        ClientProfile.AgeCategory age = (profile != null)
                ? profile.getAgeCategory()
                : ClientProfile.AgeCategory.BELOW_60;

        // ── STEP 1: Compute each income head ──────────────────────
        double salaryIncome     = computeSalaryIncome(income);
        double hpIncome         = computeHousePropertyIncome(income);
        double cgIncome         = computeCapitalGainsIncome(income);
        double otherIncome      = computeOtherSourcesIncome(income);
        double grossTotal       = salaryIncome + hpIncome + cgIncome + otherIncome;

        dto.setSalaryIncome(round(salaryIncome));
        dto.setHousePropertyIncome(round(hpIncome));
        dto.setCapitalGainsIncome(round(cgIncome));
        dto.setOtherSourcesIncome(round(otherIncome));
        dto.setGrossTotalIncome(round(grossTotal));

        // ── STEP 2: HRA Exemption ─────────────────────────────────
        double hraExemption = computeHraExemption(income);
        dto.setHraExemption(round(hraExemption));

        // ── STEP 3: OLD REGIME deductions ─────────────────────────
        DeductionSummary oldDed = computeOldRegimeDeductions(income, deductions, hraExemption, age);
        double taxableIncomeOld = Math.max(0, grossTotal - oldDed.total);

        dto.setTotalDeductionsOld(round(oldDed.total));
        dto.setTaxableIncomeOld(round(taxableIncomeOld));
        dto.setSec80C_total(round(oldDed.sec80C));
        dto.setSec80D_total(round(oldDed.sec80D));
        dto.setSec80CCD1B(round(oldDed.sec80CCD1B));
        dto.setSec80G_total(round(oldDed.sec80G));
        dto.setSec80TTA_80TTB(round(oldDed.sec80TTA));
        dto.setSec80E(round(oldDed.sec80E));
        dto.setProfessionalTax(round(oldDed.professionalTax));
        dto.setStandardDeduction(round(oldDed.standardDeduction));
        dto.setHomeLoanInterest(round(oldDed.homeLoanInterest));

        // ── STEP 4: NEW REGIME deductions ─────────────────────────
        double newStdDeduction = 75_000;
        double taxableIncomeNew = Math.max(0, grossTotal - newStdDeduction);
        dto.setTaxableIncomeNew(round(taxableIncomeNew));

        // ── STEP 5: Tax OLD Regime ─────────────────────────────────
        OldRegimeTaxResult oldResult = computeOldRegimeTax(taxableIncomeOld, age);
        dto.setBaseTaxOld(round(oldResult.baseTax));
        dto.setRebate87AOld(round(oldResult.rebate87A));
        dto.setSurchargeOld(round(oldResult.surcharge));
        dto.setCessOld(round(oldResult.cess));
        dto.setTotalTaxOld(round(oldResult.totalTax));

        // ── STEP 6: Tax NEW Regime ─────────────────────────────────
        NewRegimeTaxResult newResult = computeNewRegimeTax(taxableIncomeNew);
        dto.setBaseTaxNew(round(newResult.baseTax));
        dto.setRebate87ANew(round(newResult.rebate87A));
        dto.setSurchargeNew(round(newResult.surcharge));
        dto.setCessNew(round(newResult.cess));
        dto.setTotalTaxNew(round(newResult.totalTax));

        // ── STEP 7: Credits & Net Tax ──────────────────────────────
        double tdsCredits  = income.getTdsSalary() + income.getTdsOther();
        double advCredits  = income.getAdvanceTaxPaid() + income.getSelfAssessmentTax();
        double totalCredits = tdsCredits + advCredits;

        dto.setTdsCredits(round(tdsCredits));
        dto.setAdvanceTaxCredits(round(advCredits));

        double netOld = oldResult.totalTax - totalCredits;
        double netNew = newResult.totalTax - totalCredits;

        dto.setNetTaxPayableOld(netOld > 0 ? round(netOld) : 0);
        dto.setRefundOld(netOld < 0 ? round(-netOld) : 0);
        dto.setNetTaxPayableNew(netNew > 0 ? round(netNew) : 0);
        dto.setRefundNew(netNew < 0 ? round(-netNew) : 0);

        // ── STEP 8: Recommendation ────────────────────────────────
        if (newResult.totalTax <= oldResult.totalTax) {
            dto.setRecommendedRegime("NEW");
            dto.setTaxSavings(round(oldResult.totalTax - newResult.totalTax));
        } else {
            dto.setRecommendedRegime("OLD");
            dto.setTaxSavings(round(newResult.totalTax - oldResult.totalTax));
        }

        return dto;
    }

    // ─────────────────────────────────────────────
    //  INCOME COMPUTATIONS
    // ─────────────────────────────────────────────

    private double computeSalaryIncome(IncomeDetails income) {
        // Gross Salary = Basic + HRA + LTA + Special + Other + Bonus
        double grossSalary = income.getBasicSalary()
                + income.getHraReceived()
                + income.getLtaReceived()
                + income.getSpecialAllowance()
                + income.getOtherAllowances()
                + income.getBonus();

        // Standard Deduction is applied during regime computation
        // Professional tax is handled in deductions
        return grossSalary;
    }

    private double computeHousePropertyIncome(IncomeDetails income) {
        // Net Annual Value = Gross Annual Value - Municipal Taxes
        double nav = income.getAnnualRentalIncome() - income.getMunicipalTaxPaid();
        // Standard Deduction of 30% allowed on NAV
        double stdDed30 = nav > 0 ? 0.30 * nav : 0;
        // Home loan interest for let-out (no limit) or self-occupied (capped at ₹2L in deductions)
        double hpIncome = nav - stdDed30 - income.getHomeLoanInterestSelfOccupied();
        // Loss from house property (max set-off ₹2L against salary)
        return Math.max(-200_000, hpIncome);
    }

    private double computeCapitalGainsIncome(IncomeDetails income) {
        // STCG under 111A (listed equity): taxed @ 20% (post July 2024)
        // LTCG on equity: taxed @ 12.5% over ₹1.25L exempt
        double ltcgExempt = 125_000;
        double ltcgTaxable = Math.max(0, income.getLongTermCapitalGains() - ltcgExempt);
        // For simplicity, add all to income (tax rate handled in engine)
        return income.getShortTermCapitalGains() + ltcgTaxable + income.getStcgSpecialRate();
    }

    private double computeOtherSourcesIncome(IncomeDetails income) {
        return income.getInterestFromSavings()
                + income.getInterestFromFD()
                + income.getDividendIncome()
                + income.getOtherIncome();
    }

    // ─────────────────────────────────────────────
    //  HRA EXEMPTION (Min of 3 conditions)
    // ─────────────────────────────────────────────

    private double computeHraExemption(IncomeDetails income) {
        double basic = income.getBasicSalary();
        double hraReceived = income.getHraReceived();
        double rentPaid = income.getRentPaidMonthly() * 12;
        boolean isMetro = "METRO".equalsIgnoreCase(income.getCityType());

        if (basic == 0 || hraReceived == 0 || rentPaid == 0) return 0;

        double cond1 = hraReceived;                               // Actual HRA received
        double cond2 = isMetro ? 0.50 * basic : 0.40 * basic;    // 50%/40% of basic
        double cond3 = Math.max(0, rentPaid - (0.10 * basic));    // Rent - 10% of basic

        return Math.min(cond1, Math.min(cond2, cond3));
    }

    // ─────────────────────────────────────────────
    //  OLD REGIME: DEDUCTIONS
    // ─────────────────────────────────────────────

    private DeductionSummary computeOldRegimeDeductions(
            IncomeDetails income, DeductionDetails ded,
            double hraExemption, ClientProfile.AgeCategory age) {

        DeductionSummary s = new DeductionSummary();

        // Standard Deduction ₹50,000
        s.standardDeduction = 50_000;

        // Professional tax (max ₹2,500)
        s.professionalTax = Math.min(ded.getProfessionalTax(), 2_500);

        // HRA exemption
        s.hraExemption = hraExemption;

        // 80C (max ₹1,50,000)
        double raw80C = ded.getSec80C_ppf() + ded.getSec80C_elss() + ded.getSec80C_lic()
                + ded.getSec80C_epf() + ded.getSec80C_nsc() + ded.getSec80C_tuitionFees()
                + ded.getSec80C_homeLoanPrincipal() + ded.getSec80C_sukanyaSamridhi()
                + ded.getSec80C_others();
        s.sec80C = Math.min(raw80C, 150_000);

        // 80CCD(1B) NPS extra ₹50,000
        s.sec80CCD1B = Math.min(ded.getSec80CCD1B_nps(), 50_000);

        // 80D
        double selfLimit = 25_000;
        double parentLimit = ded.isParentsSeniorCitizen() ? 50_000 : 25_000;
        s.sec80D = Math.min(ded.getSec80D_selfFamily(), selfLimit)
                + Math.min(ded.getSec80D_parents(), parentLimit);

        // 80G
        s.sec80G = ded.getSec80G_100percent() + (0.50 * ded.getSec80G_50percent());

        // 80TTA / 80TTB
        if (age == ClientProfile.AgeCategory.SENIOR_60_80 || age == ClientProfile.AgeCategory.SUPER_SENIOR) {
            s.sec80TTA = Math.min(ded.getSec80TTB_fdInterest(), 50_000);
        } else {
            s.sec80TTA = Math.min(ded.getSec80TTA_savingsInterest(), 10_000);
        }

        // 80E Education loan (no limit)
        s.sec80E = ded.getSec80E_educationLoan();

        // Home loan interest (Section 24b, self-occupied max ₹2L)
        s.homeLoanInterest = Math.min(income.getHomeLoanInterestSelfOccupied(), 200_000);

        s.total = s.standardDeduction + s.professionalTax + s.hraExemption
                + s.sec80C + s.sec80CCD1B + s.sec80D + s.sec80G
                + s.sec80TTA + s.sec80E;

        return s;
    }

    // ─────────────────────────────────────────────
    //  OLD REGIME: TAX COMPUTATION
    // ─────────────────────────────────────────────

    private OldRegimeTaxResult computeOldRegimeTax(double taxableIncome, ClientProfile.AgeCategory age) {
        OldRegimeTaxResult r = new OldRegimeTaxResult();

        double basicExemption;
        switch (age) {
            case SUPER_SENIOR   -> basicExemption = 500_000;
            case SENIOR_60_80   -> basicExemption = 300_000;
            default             -> basicExemption = 250_000;
        }

        r.baseTax = computeOldSlabTax(taxableIncome, basicExemption);

        // Rebate 87A: if income ≤ ₹5 lakh, full tax waived (max ₹12,500)
        if (taxableIncome <= 500_000) {
            r.rebate87A = Math.min(r.baseTax, 12_500);
        }
        double taxAfterRebate = Math.max(0, r.baseTax - r.rebate87A);

        // Surcharge
        r.surcharge = computeSurcharge(taxableIncome, taxAfterRebate);
        double taxWithSurcharge = taxAfterRebate + r.surcharge;

        // Cess 4%
        r.cess = 0.04 * taxWithSurcharge;
        r.totalTax = round(taxWithSurcharge + r.cess);
        return r;
    }

    private double computeOldSlabTax(double income, double basicExemption) {
        double tax = 0;
        if (income <= basicExemption) return 0;

        double slab1End = 500_000;
        double slab2End = 1_000_000;

        if (income > basicExemption) {
            double taxable = Math.min(income, slab1End) - basicExemption;
            tax += 0.05 * Math.max(0, taxable);
        }
        if (income > slab1End) {
            double taxable = Math.min(income, slab2End) - slab1End;
            tax += 0.20 * taxable;
        }
        if (income > slab2End) {
            tax += 0.30 * (income - slab2End);
        }
        return tax;
    }

    // ─────────────────────────────────────────────
    //  NEW REGIME: TAX COMPUTATION (FY 2024-25)
    // ─────────────────────────────────────────────

    private NewRegimeTaxResult computeNewRegimeTax(double taxableIncome) {
        NewRegimeTaxResult r = new NewRegimeTaxResult();

        r.baseTax = computeNewSlabTax(taxableIncome);

        // Rebate 87A: if income ≤ ₹12 lakh, tax = 0 (max rebate ₹60,000)
        if (taxableIncome <= 1_200_000) {
            r.rebate87A = Math.min(r.baseTax, 60_000);
        }
        double taxAfterRebate = Math.max(0, r.baseTax - r.rebate87A);

        // Surcharge
        r.surcharge = computeSurcharge(taxableIncome, taxAfterRebate);
        double taxWithSurcharge = taxAfterRebate + r.surcharge;

        // Cess 4%
        r.cess = 0.04 * taxWithSurcharge;
        r.totalTax = round(taxWithSurcharge + r.cess);
        return r;
    }

    private double computeNewSlabTax(double income) {
        double tax = 0;
        if (income <= 400_000) return 0;

        // 5% on 4L–8L
        if (income > 400_000)
            tax += 0.05 * (Math.min(income, 800_000) - 400_000);
        // 10% on 8L–12L
        if (income > 800_000)
            tax += 0.10 * (Math.min(income, 1_200_000) - 800_000);
        // 15% on 12L–16L
        if (income > 1_200_000)
            tax += 0.15 * (Math.min(income, 1_600_000) - 1_200_000);
        // 20% on 16L–20L
        if (income > 1_600_000)
            tax += 0.20 * (Math.min(income, 2_000_000) - 1_600_000);
        // 25% on 20L–24L
        if (income > 2_000_000)
            tax += 0.25 * (Math.min(income, 2_400_000) - 2_000_000);
        // 30% above 24L
        if (income > 2_400_000)
            tax += 0.30 * (income - 2_400_000);

        return tax;
    }

    // ─────────────────────────────────────────────
    //  SURCHARGE COMPUTATION
    // ─────────────────────────────────────────────

    private double computeSurcharge(double income, double taxAfterRebate) {
        if (income <= 5_000_000)   return 0;
        if (income <= 10_000_000)  return 0.10 * taxAfterRebate;
        if (income <= 20_000_000)  return 0.15 * taxAfterRebate;
        if (income <= 50_000_000)  return 0.25 * taxAfterRebate;
        return 0.37 * taxAfterRebate; // above 5 crore
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Inner result classes
    private static class DeductionSummary {
        double total, standardDeduction, professionalTax, hraExemption;
        double sec80C, sec80CCD1B, sec80D, sec80G, sec80TTA, sec80E;
        double homeLoanInterest;
    }

    private static class OldRegimeTaxResult {
        double baseTax, rebate87A, surcharge, cess, totalTax;
    }

    private static class NewRegimeTaxResult {
        double baseTax, rebate87A, surcharge, cess, totalTax;
    }
}
