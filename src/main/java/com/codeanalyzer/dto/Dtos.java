package com.codeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Objects (DTOs) - These classes transfer data between
 * the frontend and backend. They are separate from the database entities.
 */
public class Dtos {

    // =============================================
    // AUTH DTOs
    // =============================================

    /** Used when user submits the login form */
    public static class LoginRequest {
        private String username;
        private String password;
    
        public LoginRequest() {}
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** Used when user registers a new account */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    
        public RegisterRequest() {}
        public RegisterRequest(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /** Sent back to frontend after successful login */
    public static class AuthResponse {
        private String token;       // JWT token
        private String username;
        private String role;
        private String message;
    
        public AuthResponse() {}
        public AuthResponse(String token, String username, String role, String message) {
            this.token = token;
            this.username = username;
            this.role = role;
            this.message = message;
        }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // =============================================
    // CODE SUBMISSION DTOs
    // =============================================

    /** Sent from frontend when user submits code for analysis */
    public static class CodeSubmitRequest {
        private String language;    // Java, C, Python
        private String code;        // The actual code
        private String stdin;       // Standard Input arguments
    
        public CodeSubmitRequest() {}
        public CodeSubmitRequest(String language, String code, String stdin) {
            this.language = language;
            this.code = code;
            this.stdin = stdin;
        }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getStdin() { return stdin; }
        public void setStdin(String stdin) { this.stdin = stdin; }
    }

    /** Full analysis result sent back to frontend */
    public static class AnalysisResponse {
        private Long submissionId;

        // Execution results
        private String output;
        private String errorOutput;
        private boolean executionSuccess;

        // Code metrics
        private int lineCount;
        private int cyclomaticComplexity;
        private int methodCount;
        private int variableCount;
        private int constructorCount;

        // Explanations
        private String beginnerExplanation;
        private String workflowExplanation;
        private String aiExplanation;

        // Video suggestions
        private List<VideoSuggestion> videoSuggestions;
    
        public AnalysisResponse() {}
        public AnalysisResponse(Long submissionId, String output, String errorOutput, boolean executionSuccess, int lineCount, int cyclomaticComplexity, int methodCount, int variableCount, int constructorCount, String beginnerExplanation, String workflowExplanation, String aiExplanation, List<VideoSuggestion> videoSuggestions) {
            this.submissionId = submissionId;
            this.output = output;
            this.errorOutput = errorOutput;
            this.executionSuccess = executionSuccess;
            this.lineCount = lineCount;
            this.cyclomaticComplexity = cyclomaticComplexity;
            this.methodCount = methodCount;
            this.variableCount = variableCount;
            this.constructorCount = constructorCount;
            this.beginnerExplanation = beginnerExplanation;
            this.workflowExplanation = workflowExplanation;
            this.aiExplanation = aiExplanation;
            this.videoSuggestions = videoSuggestions;
        }
        public Long getSubmissionId() { return submissionId; }
        public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getErrorOutput() { return errorOutput; }
        public void setErrorOutput(String errorOutput) { this.errorOutput = errorOutput; }
        public boolean isExecutionSuccess() { return executionSuccess; }
        public void setExecutionSuccess(boolean executionSuccess) { this.executionSuccess = executionSuccess; }
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
        public int getCyclomaticComplexity() { return cyclomaticComplexity; }
        public void setCyclomaticComplexity(int cyclomaticComplexity) { this.cyclomaticComplexity = cyclomaticComplexity; }
        public int getMethodCount() { return methodCount; }
        public void setMethodCount(int methodCount) { this.methodCount = methodCount; }
        public int getVariableCount() { return variableCount; }
        public void setVariableCount(int variableCount) { this.variableCount = variableCount; }
        public int getConstructorCount() { return constructorCount; }
        public void setConstructorCount(int constructorCount) { this.constructorCount = constructorCount; }
        public String getBeginnerExplanation() { return beginnerExplanation; }
        public void setBeginnerExplanation(String beginnerExplanation) { this.beginnerExplanation = beginnerExplanation; }
        public String getWorkflowExplanation() { return workflowExplanation; }
        public void setWorkflowExplanation(String workflowExplanation) { this.workflowExplanation = workflowExplanation; }
        public String getAiExplanation() { return aiExplanation; }
        public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
        public List<VideoSuggestion> getVideoSuggestions() { return videoSuggestions; }
        public void setVideoSuggestions(List<VideoSuggestion> videoSuggestions) { this.videoSuggestions = videoSuggestions; }
    }

    /** Represents a YouTube video suggestion */
    public static class VideoSuggestion {
        private String title;
        private String url;
        private String description;
    
        public VideoSuggestion() {}
        public VideoSuggestion(String title, String url, String description) {
            this.title = title;
            this.url = url;
            this.description = description;
        }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // =============================================
    // AI CHAT DTOs
    // =============================================

    /** User sends a question about their code */
    public static class AiChatRequest {
        private String question;    // User's question
        private String code;        // The code context
        private String language;    // Language of the code
    
        public AiChatRequest() {}
        public AiChatRequest(String question, String code, String language) {
            this.question = question;
            this.code = code;
            this.language = language;
        }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    /** AI response sent back to frontend */
    public static class AiChatResponse {
        private String answer;
        private boolean success;
    
        public AiChatResponse() {}
        public AiChatResponse(String answer, boolean success) {
            this.answer = answer;
            this.success = success;
        }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    // =============================================
    // HISTORY DTOs
    // =============================================

    /** Summary of a past submission for history page */
    public static class SubmissionSummary {
        private Long id;
        private String language;
        private String codePreview;     // First 100 chars of code
        private boolean executionSuccess;
        private int lineCount;
        private LocalDateTime submittedAt;
    
        public SubmissionSummary() {}
        public SubmissionSummary(Long id, String language, String codePreview, boolean executionSuccess, int lineCount, LocalDateTime submittedAt) {
            this.id = id;
            this.language = language;
            this.codePreview = codePreview;
            this.executionSuccess = executionSuccess;
            this.lineCount = lineCount;
            this.submittedAt = submittedAt;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCodePreview() { return codePreview; }
        public void setCodePreview(String codePreview) { this.codePreview = codePreview; }
        public boolean isExecutionSuccess() { return executionSuccess; }
        public void setExecutionSuccess(boolean executionSuccess) { this.executionSuccess = executionSuccess; }
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    }

    // =============================================
    // ADMIN DTOs
    // =============================================

    /** Admin dashboard analytics */
    public static class AdminAnalytics {
        private long totalUsers;
        private long totalSubmissions;
        private long successfulExecutions;
        private long failedExecutions;
        private long javaSubmissions;
        private long cSubmissions;
        private long pythonSubmissions;
    
        public AdminAnalytics() {}
        public AdminAnalytics(long totalUsers, long totalSubmissions, long successfulExecutions, long failedExecutions, long javaSubmissions, long cSubmissions, long pythonSubmissions) {
            this.totalUsers = totalUsers;
            this.totalSubmissions = totalSubmissions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.javaSubmissions = javaSubmissions;
            this.cSubmissions = cSubmissions;
            this.pythonSubmissions = pythonSubmissions;
        }
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(long totalSubmissions) { this.totalSubmissions = totalSubmissions; }
        public long getSuccessfulExecutions() { return successfulExecutions; }
        public void setSuccessfulExecutions(long successfulExecutions) { this.successfulExecutions = successfulExecutions; }
        public long getFailedExecutions() { return failedExecutions; }
        public void setFailedExecutions(long failedExecutions) { this.failedExecutions = failedExecutions; }
        public long getJavaSubmissions() { return javaSubmissions; }
        public void setJavaSubmissions(long javaSubmissions) { this.javaSubmissions = javaSubmissions; }
        public long getCSubmissions() { return cSubmissions; }
        public void setCSubmissions(long cSubmissions) { this.cSubmissions = cSubmissions; }
        public long getPythonSubmissions() { return pythonSubmissions; }
        public void setPythonSubmissions(long pythonSubmissions) { this.pythonSubmissions = pythonSubmissions; }
    }

    /** User info for admin view */
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String role;
        private boolean active;
        private LocalDateTime createdAt;
        private long submissionCount;
    
        public UserInfo() {}
        public UserInfo(Long id, String username, String email, String role, boolean active, LocalDateTime createdAt, long submissionCount) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.active = active;
            this.createdAt = createdAt;
            this.submissionCount = submissionCount;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public long getSubmissionCount() { return submissionCount; }
        public void setSubmissionCount(long submissionCount) { this.submissionCount = submissionCount; }
    }
}
