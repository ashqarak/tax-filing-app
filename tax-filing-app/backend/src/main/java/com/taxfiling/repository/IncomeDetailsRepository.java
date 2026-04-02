package com.taxfiling.repository;

import com.taxfiling.model.IncomeDetails;
import com.taxfiling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IncomeDetailsRepository extends JpaRepository<IncomeDetails, Long> {
    Optional<IncomeDetails> findByUserAndAssessmentYear(User user, String assessmentYear);
}
