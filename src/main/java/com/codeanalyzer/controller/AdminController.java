package com.codeanalyzer.controller;

import com.codeanalyzer.dto.Dtos.*;
import com.codeanalyzer.entity.CodeSubmission;
import com.codeanalyzer.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController - Endpoints only accessible by ADMIN role users.
 *
 * Endpoints:
 * GET  /api/admin/analytics       - Dashboard stats
 * GET  /api/admin/users           - All user list
 * GET  /api/admin/submissions     - All code submissions
 * PUT  /api/admin/users/{id}/toggle - Enable/disable user
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")  // All endpoints in this controller require ADMIN role
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Get analytics summary for admin dashboard.
     * URL: GET /api/admin/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalytics> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }

    /**
     * Get list of all registered users.
     * URL: GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * Get all code submissions (for admin review).
     * URL: GET /api/admin/submissions
     */
    @GetMapping("/submissions")
    public ResponseEntity<List<CodeSubmission>> getAllSubmissions() {
        return ResponseEntity.ok(adminService.getAllSubmissions());
    }

    /**
     * Enable or disable a user's account.
     * URL: PUT /api/admin/users/{id}/toggle
     */
    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long id) {
        try {
            String result = adminService.toggleUserStatus(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
