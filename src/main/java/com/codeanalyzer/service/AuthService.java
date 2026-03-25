package com.codeanalyzer.service;

import com.codeanalyzer.dto.Dtos.*;
import com.codeanalyzer.entity.User;
import com.codeanalyzer.repository.UserRepository;
import com.codeanalyzer.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService - Handles user login and registration logic.
 *
 * Responsibilities:
 * 1. Register new users (save to MySQL with hashed password)
 * 2. Login existing users (verify password, return JWT token)
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Register a new user account.
     *
     * @param request - contains username, email, password
     * @return success/failure message
     */
    public String register(RegisterRequest request) {

        // Check if username already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            return "Error: Username '" + request.getUsername() + "' is already taken!";
        }

        // Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Error: Email '" + request.getEmail() + "' is already registered!";
        }

        // Create new User entity
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());

        // IMPORTANT: Never store plain text passwords!
        // BCryptPasswordEncoder converts "password123" → "$2a$10$xyz..."
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole("USER"); // Default role is USER

        // Save to MySQL database
        userRepository.save(newUser);

        return "User registered successfully! Please login.";
    }

    /**
     * Authenticate user login and return JWT token.
     *
     * @param request - contains username and password
     * @return AuthResponse with JWT token and user info
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate using Spring Security (checks username + hashed password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If we reach here, authentication was successful!
        // Load full user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Generate JWT token
        String token = jwtUtils.generateToken(userDetails);

        // Get user's role from database
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        // Return token + user info to frontend
        return new AuthResponse(token, user.getUsername(), user.getRole(), "Login successful!");
    }
}
