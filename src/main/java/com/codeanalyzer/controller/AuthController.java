package com.codeanalyzer.controller;

import com.codeanalyzer.dto.Dtos.*;
import com.codeanalyzer.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - Handles login and registration API endpoints.
 *
 * Endpoints:
 * POST /api/auth/register - Create new account
 * POST /api/auth/login    - Login and get JWT token
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user account.
     * URL: POST /api/auth/register
     * Body: { "username": "john", "email": "john@email.com", "password": "pass123" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Password must be at least 6 characters");
            }
            if (request.getEmail() == null || !request.getEmail().contains("@")) {
                return ResponseEntity.badRequest().body("Valid email is required");
            }

            String result = authService.register(request);

            if (result.startsWith("Error:")) {
                return ResponseEntity.badRequest().body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login with username and password, get JWT token back.
     * URL: POST /api/auth/login
     * Body: { "username": "john", "password": "pass123" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
