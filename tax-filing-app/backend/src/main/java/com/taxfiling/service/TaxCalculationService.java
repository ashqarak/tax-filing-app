package com.taxfiling.service;

import com.taxfiling.dto.TaxSummaryDTO;
import com.taxfiling.engine.IndianTaxEngine;
import com.taxfiling.model.*;
import com.taxfiling.repository.TaxReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private final IndianTaxEngine taxEngine;
    private final TaxReturnRepository taxReturnRepository;
    private final IncomeService incomeService;
    private final DeductionService deductionService;
    private final ClientProfileService profileService;

    public TaxSummaryDTO calculate(User user, String ay) {
        IncomeDetails income = incomeService.getOrCreate(user, ay);
        DeductionDetails deductions = deductionService.getOrCreate(user, ay);
        ClientProfile profile = profileService.getOrCreate(user);

        TaxSummaryDTO summary = taxEngine.compute(income, deductions, profile, user.getName());

        // Persist the tax return snapshot
        TaxReturn tr = taxReturnRepository.findByUserAndAssessmentYear(user, ay)
                .orElse(new TaxReturn());
        tr.setUser(user);
        tr.setAssessmentYear(ay);
        tr.setGrossTotalIncome(summary.getGrossTotalIncome());
        tr.setTotalDeductions(summary.getTotalDeductionsOld());
        tr.setTaxableIncomeOldRegime(summary.getTaxableIncomeOld());
        tr.setTaxableIncomeNewRegime(summary.getTaxableIncomeNew());
        tr.setTaxOldRegime(summary.getBaseTaxOld());
        tr.setTaxNewRegime(summary.getBaseTaxNew());
        tr.setSurchargeOld(summary.getSurchargeOld());
        tr.setSurchargeNew(summary.getSurchargeNew());
        tr.setCessOld(summary.getCessOld());
        tr.setCessNew(summary.getCessNew());
        tr.setTotalTaxOld(summary.getTotalTaxOld());
        tr.setTotalTaxNew(summary.getTotalTaxNew());
        tr.setTdsCredits(summary.getTdsCredits());
        tr.setAdvanceTaxCredits(summary.getAdvanceTaxCredits());
        tr.setNetTaxPayable(
                "NEW".equals(summary.getRecommendedRegime())
                        ? summary.getNetTaxPayableNew()
                        : summary.getNetTaxPayableOld()
        );
        tr.setRefundAmount(
                "NEW".equals(summary.getRecommendedRegime())
                        ? summary.getRefundNew()
                        : summary.getRefundOld()
        );
        tr.setRecommendedRegime(summary.getRecommendedRegime());
        tr.setTaxSavings(summary.getTaxSavings());
        if (tr.getStatus() == null) tr.setStatus(TaxReturn.Status.DRAFT);
        taxReturnRepository.save(tr);

        return summary;
    }

    public TaxReturn submitReturn(User user, String ay) {
        TaxReturn tr = taxReturnRepository.findByUserAndAssessmentYear(user, ay)
                .orElseThrow(() -> new RuntimeException("Please calculate tax first for AY " + ay));
        tr.setStatus(TaxReturn.Status.UNDER_REVIEW);
        tr.setSubmittedAt(LocalDateTime.now());
        return taxReturnRepository.save(tr);
    }

    public List<TaxReturn> getAllReturns(User user) {
        return taxReturnRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public TaxReturn getReturn(User user, String ay) {
        return taxReturnRepository.findByUserAndAssessmentYear(user, ay)
                .orElseThrow(() -> new RuntimeException("No return found for AY " + ay));
    }
}
