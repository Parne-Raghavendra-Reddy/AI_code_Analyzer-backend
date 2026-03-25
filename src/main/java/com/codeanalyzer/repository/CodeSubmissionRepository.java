package com.codeanalyzer.repository;

import com.codeanalyzer.entity.CodeSubmission;
import com.codeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CodeSubmissionRepository - handles all database operations for code submissions.
 */
@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {

    // Get all submissions for a specific user (their history)
    List<CodeSubmission> findByUserOrderBySubmittedAtDesc(User user);

    // Get all submissions for admin dashboard
    List<CodeSubmission> findAllByOrderBySubmittedAtDesc();

    // Count submissions per user
    long countByUser(User user);

    // Get submissions by language
    List<CodeSubmission> findByLanguage(String language);

    // Count total successful executions
    long countByExecutionSuccess(boolean success);

    // Get recent submissions (for admin analytics)
    @Query("SELECT s FROM CodeSubmission s ORDER BY s.submittedAt DESC")
    List<CodeSubmission> findRecentSubmissions();
}
