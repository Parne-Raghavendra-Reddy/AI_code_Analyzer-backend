package com.codeanalyzer.service;

import com.codeanalyzer.dto.Dtos.*;
import com.codeanalyzer.entity.CodeSubmission;
import com.codeanalyzer.entity.User;
import com.codeanalyzer.repository.CodeSubmissionRepository;
import com.codeanalyzer.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CodeAnalyzerService - The main orchestrator service.
 *
 * When a user submits code, this service:
 * 1. Executes the code
 * 2. Analyzes its structure
 * 3. Generates explanations
 * 4. Gets AI insights
 * 5. Suggests videos
 * 6. Saves everything to MySQL
 * 7. Returns a complete response
 */
@Service
public class CodeAnalyzerService {

    @Autowired
    private CodeExecutionService executionService;

    @Autowired
    private CodeAnalysisService analysisService;

    @Autowired
    private AiService aiService;

    @Autowired
    private CodeSubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Main method called when user submits code.
     *
     * @param request        - contains language and code
     * @param authentication - Spring Security auth (who's logged in)
     * @return complete analysis response
     */
    public AnalysisResponse analyzeCode(CodeSubmitRequest request, Authentication authentication) {

        String code = request.getCode();
        String language = request.getLanguage();

        // ============================================
        // STEP 1: Execute the code
        // ============================================
        CodeExecutionService.ExecutionResult executionResult =
                executionService.executeCode(language, code, request.getStdin());

        // ============================================
        // STEP 2: Calculate code metrics
        // ============================================
        int lineCount = analysisService.countLines(code);
        int complexity = analysisService.calculateCyclomaticComplexity(code);
        int methodCount = analysisService.countMethods(code, language);
        int variableCount = analysisService.countVariables(code, language);
        int constructorCount = analysisService.countConstructors(code, language);

        // ============================================
        // STEP 3: Generate beginner explanation
        // ============================================
        String beginnerExplanation = analysisService.generateBeginnerExplanation(
                code, language, executionResult.output);

        // ============================================
        // STEP 4: Generate workflow explanation
        // ============================================
        String workflowExplanation = analysisService.generateWorkflowExplanation(
                code, language, executionResult.output);

        // ============================================
        // STEP 5: Get AI explanation (if API configured)
        // ============================================
        String aiExplanation = aiService.explainCode(code, language);

        // ============================================
        // STEP 6: Get video suggestions
        // ============================================
        List<VideoSuggestion> videoSuggestions = analysisService.suggestVideos(code, language);

        // ============================================
        // STEP 7: Save to MySQL database
        // ============================================
        CodeSubmission submission = new CodeSubmission();
        submission.setLanguage(language);
        submission.setCode(code);
        submission.setOutput(executionResult.output);
        submission.setErrorOutput(executionResult.errorOutput);
        submission.setExecutionSuccess(executionResult.success);
        submission.setLineCount(lineCount);
        submission.setCyclomaticComplexity(complexity);
        submission.setMethodCount(methodCount);
        submission.setVariableCount(variableCount);
        submission.setConstructorCount(constructorCount);
        submission.setBeginnerExplanation(beginnerExplanation);
        submission.setWorkflowExplanation(workflowExplanation);
        submission.setAiExplanation(aiExplanation);

        // Serialize video suggestions to JSON string for storage
        try {
            submission.setVideoSuggestions(objectMapper.writeValueAsString(videoSuggestions));
        } catch (JsonProcessingException e) {
            submission.setVideoSuggestions("[]");
        }

        // Link submission to the logged-in user
        if (authentication != null) {
            userRepository.findByUsername(authentication.getName())
                    .ifPresent(submission::setUser);
        }

        CodeSubmission saved = submissionRepository.save(submission);

        // ============================================
        // STEP 8: Build and return response
        // ============================================
        AnalysisResponse response = new AnalysisResponse();
        response.setSubmissionId(saved.getId());
        response.setOutput(executionResult.output);
        response.setErrorOutput(executionResult.errorOutput);
        response.setExecutionSuccess(executionResult.success);
        response.setLineCount(lineCount);
        response.setCyclomaticComplexity(complexity);
        response.setMethodCount(methodCount);
        response.setVariableCount(variableCount);
        response.setConstructorCount(constructorCount);
        response.setBeginnerExplanation(beginnerExplanation);
        response.setWorkflowExplanation(workflowExplanation);
        response.setAiExplanation(aiExplanation);
        response.setVideoSuggestions(videoSuggestions);

        return response;
    }

    /**
     * Get code submission history for the logged-in user.
     */
    public List<SubmissionSummary> getUserHistory(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CodeSubmission> submissions = submissionRepository.findByUserOrderBySubmittedAtDesc(user);
        List<SubmissionSummary> summaries = new ArrayList<>();

        for (CodeSubmission sub : submissions) {
            // Show only first 100 characters of code as preview
            String preview = sub.getCode().length() > 100
                    ? sub.getCode().substring(0, 100) + "..."
                    : sub.getCode();

            summaries.add(new SubmissionSummary(
                    sub.getId(),
                    sub.getLanguage(),
                    preview,
                    sub.isExecutionSuccess(),
                    sub.getLineCount(),
                    sub.getSubmittedAt()
            ));
        }

        return summaries;
    }

    /**
     * Get a specific submission by ID.
     */
    public CodeSubmission getSubmissionById(Long id, Authentication authentication) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));
    }
}
