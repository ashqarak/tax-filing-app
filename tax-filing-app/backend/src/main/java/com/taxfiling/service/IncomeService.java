package com.taxfiling.service;

import com.taxfiling.model.IncomeDetails;
import com.taxfiling.model.User;
import com.taxfiling.repository.IncomeDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeDetailsRepository incomeRepo;

    public IncomeDetails getOrCreate(User user, String ay) {
        return incomeRepo.findByUserAndAssessmentYear(user, ay)
                .orElseGet(() -> {
                    IncomeDetails inc = new IncomeDetails();
                    inc.setUser(user);
                    inc.setAssessmentYear(ay);
                    return incomeRepo.save(inc);
                });
    }

    public IncomeDetails save(User user, String ay, IncomeDetails incoming) {
        IncomeDetails inc = incomeRepo.findByUserAndAssessmentYear(user, ay)
                .orElse(new IncomeDetails());
        incoming.setId(inc.getId());
        incoming.setUser(user);
        incoming.setAssessmentYear(ay);
        return incomeRepo.save(incoming);
    }
}
