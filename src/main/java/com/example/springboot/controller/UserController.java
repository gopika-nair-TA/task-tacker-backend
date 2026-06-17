package com.example.springboot.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.springboot.dao.SyncUserRequest;
import com.example.springboot.service.UserAuthService;

@RestController
@RequestMapping("/api") 
@CrossOrigin(origins = "http://52.91.4.139.sslip.io", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserAuthService userAuthService;

    // --- 1. SYNC USER & LOGIN ENDPOINT ---
    @PostMapping("/users/sync")
    public ResponseEntity<Map<String, Object>> syncUser(@RequestBody SyncUserRequest request) {
        Map<String, Object> response = userAuthService.syncUserAndGenerateTokens(
            request.getEmail(), 
            request.getName()
        );
        return ResponseEntity.ok(response);
    }
   
    // --- 2. DEDICATED ROLE ENDPOINT ---
    @GetMapping("/role")
    public ResponseEntity<Map<String, Object>> getUserRole(@RequestParam String email) {
        Map<String, Object> response = userAuthService.getUserRoleMap(email);
        return ResponseEntity.ok(response);
    }

    // --- 3. REFRESH TOKEN ENDPOINT ---
    @PostMapping("/auth/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token missing from request payload.");
        }

        Map<String, String> response = userAuthService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}