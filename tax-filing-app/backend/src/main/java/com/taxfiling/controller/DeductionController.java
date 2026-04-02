package com.taxfiling.controller;

import com.taxfiling.model.DeductionDetails;
import com.taxfiling.model.User;
import com.taxfiling.service.AuthService;
import com.taxfiling.service.DeductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deductions")
@RequiredArgsConstructor
public class DeductionController {

    private final AuthService authService;
    private final DeductionService deductionService;

    @GetMapping("/{ay}")
    public ResponseEntity<DeductionDetails> getDeductions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(deductionService.getOrCreate(user, ay));
    }

    @PostMapping("/{ay}")
    public ResponseEntity<DeductionDetails> saveDeductions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String ay,
            @RequestBody DeductionDetails incoming) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(deductionService.save(user, ay, incoming));
    }
}
