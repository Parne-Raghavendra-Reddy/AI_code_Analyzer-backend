package com.codeanalyzer.controller;

import com.codeanalyzer.dto.Dtos.*;
import com.codeanalyzer.entity.CodeSubmission;
import com.codeanalyzer.service.AiService;
import com.codeanalyzer.service.CodeAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CodeAnalyzerController - Main controller for code submission and analysis.
 *
 * Endpoints:
 * POST /api/code/analyze      - Submit code for analysis
 * GET  /api/code/history      - Get user's submission history
 * GET  /api/code/submission/{id} - Get specific submission details
 * POST /api/code/ai-chat      - Ask AI a question about code
 */
@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeAnalyzerController {

    @Autowired
    private CodeAnalyzerService codeAnalyzerService;

    @Autowired
    private AiService aiService;

    /**
     * Submit code for execution and full analysis.
     * URL: POST /api/code/analyze
     * Header: Authorization: Bearer <jwt_token>
     * Body: { "language": "Java", "code": "public class..." }
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCode(@RequestBody CodeSubmitRequest request,
                                         Authentication authentication) {
        try {
            // Validate request
            if (request.getCode() == null || request.getCode().isBlank()) {
                return ResponseEntity.badRequest().body("Code cannot be empty");
            }
            if (request.getLanguage() == null ||
                !List.of("Java", "C", "Python").contains(request.getLanguage())) {
                return ResponseEntity.badRequest().body("Language must be Java, C, or Python");
            }

            // Run the full analysis pipeline
            AnalysisResponse response = codeAnalyzerService.analyzeCode(request, authentication);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Analysis failed: " + e.getMessage());
        }
    }

    /**
     * Get logged-in user's code submission history.
     * URL: GET /api/code/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            List<SubmissionSummary> history = codeAnalyzerService.getUserHistory(authentication);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Could not fetch history: " + e.getMessage());
        }
    }

    /**
     * Get full details of a specific submission.
     * URL: GET /api/code/submission/{id}
     */
    @GetMapping("/submission/{id}")
    public ResponseEntity<?> getSubmission(@PathVariable Long id,
                                           Authentication authentication) {
        try {
            CodeSubmission submission = codeAnalyzerService.getSubmissionById(id, authentication);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ask AI assistant a question about code.
     * URL: POST /api/code/ai-chat
     * Body: { "question": "What does this do?", "code": "...", "language": "Java" }
     */
    @PostMapping("/ai-chat")
    public ResponseEntity<?> aiChat(@RequestBody AiChatRequest request,
                                    Authentication authentication) {
        try {
            if (request.getQuestion() == null || request.getQuestion().isBlank()) {
                return ResponseEntity.badRequest().body("Question cannot be empty");
            }

            String answer = aiService.answerQuestion(
                    request.getQuestion(),
                    request.getCode(),
                    request.getLanguage()
            );

            return ResponseEntity.ok(new AiChatResponse(answer, true));

        } catch (Exception e) {
            return ResponseEntity.ok(new AiChatResponse(
                    "Sorry, I couldn't process your question: " + e.getMessage(), false));
        }
    }
}
