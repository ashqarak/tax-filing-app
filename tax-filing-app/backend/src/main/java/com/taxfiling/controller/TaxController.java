package com.taxfiling.controller;

import com.taxfiling.dto.TaxSummaryDTO;
import com.taxfiling.model.TaxReturn;
import com.taxfiling.model.User;
import com.taxfiling.service.AuthService;
import com.taxfiling.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {

    private final AuthService authService;
    private final TaxCalculationService taxService;

    /** Run the tax engine and return the full summary */
    @GetMapping("/calculate/{ay}")
    public ResponseEntity<TaxSummaryDTO> calculate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(taxService.calculate(user, ay));
    }

    /** Submit the return for review */
    @PostMapping("/submit/{ay}")
    public ResponseEntity<TaxReturn> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(taxService.submitReturn(user, ay));
    }

    /** Get a specific return */
    @GetMapping("/return/{ay}")
    public ResponseEntity<TaxReturn> getReturn(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(taxService.getReturn(user, ay));
    }

    /** Get all returns for this user */
    @GetMapping("/returns")
    public ResponseEntity<List<TaxReturn>> getAllReturns(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(taxService.getAllReturns(user));
    }
}
