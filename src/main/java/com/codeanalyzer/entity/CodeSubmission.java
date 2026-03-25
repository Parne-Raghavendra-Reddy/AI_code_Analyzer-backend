package com.codeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * CodeSubmission entity - stores every code submission made by a user.
 * Includes the code, output, errors, and analysis results.
 */
@Entity
@Table(name = "code_submissions")
public class CodeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The programming language selected (Java, C, Python)
    @Column(nullable = false, length = 20)
    private String language;

    // The actual code submitted by the user
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    // Output produced by running the code
    @Column(columnDefinition = "TEXT")
    private String output;

    // Any errors from compilation or execution
    @Column(name = "error_output", columnDefinition = "TEXT")
    private String errorOutput;

    // Whether execution was successful
    @Column(name = "execution_success")
    private boolean executionSuccess;

    // Number of lines in the code
    @Column(name = "line_count")
    private int lineCount;

    // Cyclomatic complexity score
    @Column(name = "cyclomatic_complexity")
    private int cyclomaticComplexity;

    // Number of methods detected
    @Column(name = "method_count")
    private int methodCount;

    // Number of variables detected
    @Column(name = "variable_count")
    private int variableCount;

    // Number of constructors detected
    @Column(name = "constructor_count")
    private int constructorCount;

    // Step-by-step beginner explanation
    @Column(name = "beginner_explanation", columnDefinition = "TEXT")
    private String beginnerExplanation;

    // Workflow explanation showing how output was produced
    @Column(name = "workflow_explanation", columnDefinition = "TEXT")
    private String workflowExplanation;

    // AI-generated explanation
    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    // YouTube video suggestions (stored as JSON string)
    @Column(name = "video_suggestions", columnDefinition = "TEXT")
    private String videoSuggestions;

    // When was this code submitted
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    // The user who submitted this code (Foreign Key)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    public CodeSubmission() {
    }

    public CodeSubmission(Long id, String language, String code, String output, String errorOutput, boolean executionSuccess, int lineCount, int cyclomaticComplexity, int methodCount, int variableCount, int constructorCount, String beginnerExplanation, String workflowExplanation, String aiExplanation, String videoSuggestions, LocalDateTime submittedAt, User user) {
        this.id = id;
        this.language = language;
        this.code = code;
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
        this.submittedAt = submittedAt;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

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

    public String getVideoSuggestions() { return videoSuggestions; }
    public void setVideoSuggestions(String videoSuggestions) { this.videoSuggestions = videoSuggestions; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
