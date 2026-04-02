package com.taxfiling.controller;

import com.taxfiling.model.ClientProfile;
import com.taxfiling.model.User;
import com.taxfiling.service.AuthService;
import com.taxfiling.service.ClientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final AuthService authService;
    private final ClientProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<ClientProfile> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(profileService.getOrCreate(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ClientProfile> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ClientProfile incoming) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(profileService.save(user, incoming));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(java.util.Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt().toString()
        ));
    }
}
