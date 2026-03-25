package com.codeanalyzer.service;

import com.codeanalyzer.dto.Dtos.*;
import com.codeanalyzer.entity.CodeSubmission;
import com.codeanalyzer.entity.User;
import com.codeanalyzer.repository.CodeSubmissionRepository;
import com.codeanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminService - Provides data for the admin dashboard.
 * Only accessible by users with ADMIN role.
 */
@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodeSubmissionRepository submissionRepository;

    /**
     * Get analytics summary for admin dashboard.
     */
    public AdminAnalytics getAnalytics() {
        long totalUsers = userRepository.count();
        long totalSubmissions = submissionRepository.count();
        long successful = submissionRepository.countByExecutionSuccess(true);
        long failed = totalSubmissions - successful;

        // Count by language
        long javaCount = submissionRepository.findByLanguage("Java").size();
        long cCount = submissionRepository.findByLanguage("C").size();
        long pythonCount = submissionRepository.findByLanguage("Python").size();

        return new AdminAnalytics(totalUsers, totalSubmissions, successful, failed,
                                  javaCount, cCount, pythonCount);
    }

    /**
     * Get list of all users for admin.
     */
    public List<UserInfo> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfo> userInfoList = new ArrayList<>();

        for (User user : users) {
            long submissionCount = submissionRepository.countByUser(user);
            userInfoList.add(new UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.isActive(),
                    user.getCreatedAt(),
                    submissionCount
            ));
        }
        return userInfoList;
    }

    /**
     * Get all code submissions for admin.
     */
    @Transactional(readOnly = true)
    public List<CodeSubmission> getAllSubmissions() {
        return submissionRepository.findAllByOrderBySubmittedAtDesc();
    }

    /**
     * Toggle user active status (enable/disable account).
     */
    public String toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(!user.isActive());
        userRepository.save(user);

        return "User " + user.getUsername() + " is now " +
               (user.isActive() ? "ACTIVE" : "DISABLED");
    }
}
