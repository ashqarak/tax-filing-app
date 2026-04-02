package com.taxfiling.repository;

import com.taxfiling.model.TaxReturn;
import com.taxfiling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaxReturnRepository extends JpaRepository<TaxReturn, Long> {
    Optional<TaxReturn> findByUserAndAssessmentYear(User user, String assessmentYear);
    List<TaxReturn> findByUserOrderByCreatedAtDesc(User user);
}
