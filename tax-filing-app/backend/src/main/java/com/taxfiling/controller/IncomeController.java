package com.taxfiling.controller;

import com.taxfiling.model.IncomeDetails;
import com.taxfiling.model.User;
import com.taxfiling.service.AuthService;
import com.taxfiling.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeController {

    private final AuthService authService;
    private final IncomeService incomeService;

    @GetMapping("/{ay}")
    public ResponseEntity<IncomeDetails> getIncome(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(incomeService.getOrCreate(user, ay));
    }

    @PostMapping("/{ay}")
    public ResponseEntity<IncomeDetails> saveIncome(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay,
            @RequestBody IncomeDetails incoming) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(incomeService.save(user, ay, incoming));
    }
}
