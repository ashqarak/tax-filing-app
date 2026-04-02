package com.taxfiling.service;

import com.taxfiling.model.DeductionDetails;
import com.taxfiling.model.User;
import com.taxfiling.repository.DeductionDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeductionService {

    private final DeductionDetailsRepository deductionRepo;

    public DeductionDetails getOrCreate(User user, String ay) {
        return deductionRepo.findByUserAndAssessmentYear(user, ay)
                .orElseGet(() -> {
                    DeductionDetails d = new DeductionDetails();
                    d.setUser(user);
                    d.setAssessmentYear(ay);
                    return deductionRepo.save(d);
                });
    }

    public DeductionDetails save(User user, String ay, DeductionDetails incoming) {
        DeductionDetails existing = deductionRepo.findByUserAndAssessmentYear(user, ay)
                .orElse(new DeductionDetails());
        incoming.setId(existing.getId());
        incoming.setUser(user);
        incoming.setAssessmentYear(ay);
        return deductionRepo.save(incoming);
    }
}
