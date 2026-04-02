package com.taxfiling.repository;

import com.taxfiling.model.DeductionDetails;
import com.taxfiling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeductionDetailsRepository extends JpaRepository<DeductionDetails, Long> {
    Optional<DeductionDetails> findByUserAndAssessmentYear(User user, String assessmentYear);
}
