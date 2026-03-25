package com.codeanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * AiService - Integrates with Claude AI API for intelligent code explanations.
 *
 * This service sends code to the Claude API and gets back:
 * 1. Plain English explanations for beginners
 * 2. Answers to user questions about code
 * 3. Code analysis insights
 *
 * If API key is not configured, it falls back to rule-based explanations.
 */
@Service
public class AiService {

    @Value("${app.ai.api.key:}")
    private String apiKey;

    @Value("${app.ai.api.url:https://api.anthropic.com/v1/messages}")
    private String apiUrl;

    @Value("${app.ai.model:claude-sonnet-4-20250514}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get AI explanation for the submitted code.
     *
     * @param code     - the source code
     * @param language - programming language
     * @return AI-generated beginner explanation
     */
    public String explainCode(String code, String language) {
        // If API key not configured, use fallback explanation
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_ANTHROPIC_API_KEY_HERE")) {
            return getFallbackExplanation(code, language);
        }

        String prompt = buildExplainPrompt(code, language);
        return callClaudeApi(prompt);
    }

    /**
     * Answer a user's question about their code.
     *
     * @param question - what the user wants to know
     * @param code     - the code they're asking about
     * @param language - programming language
     * @return AI answer
     */
    public String answerQuestion(String question, String code, String language) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_ANTHROPIC_API_KEY_HERE")) {
            return getFallbackAnswer(question, code, language);
        }

        String prompt = buildQuestionPrompt(question, code, language);
        return callClaudeApi(prompt);
    }

    /**
     * Build the prompt for code explanation.
     */
    private String buildExplainPrompt(String code, String language) {
        return "You are a friendly coding teacher explaining code to a complete beginner.\n\n" +
               "Please explain this " + language + " code step by step:\n\n" +
               "```" + language.toLowerCase() + "\n" + code + "\n```\n\n" +
               "Instructions:\n" +
               "1. Explain what the code does in simple English\n" +
               "2. Go line by line for important parts\n" +
               "3. Explain what each variable stores\n" +
               "4. Explain what each function/method does\n" +
               "5. Use simple words, avoid jargon\n" +
               "6. Add emojis to make it friendly\n" +
               "7. End with 'What you learned:' section\n\n" +
               "Keep it beginner-friendly and encouraging!";
    }

    /**
     * Build the prompt for answering a specific question.
     */
    private String buildQuestionPrompt(String question, String code, String language) {
        return "You are a helpful coding assistant. A beginner is learning " + language + " programming.\n\n" +
               "Here is their code:\n\n" +
               "```" + language.toLowerCase() + "\n" + code + "\n```\n\n" +
               "Their question: " + question + "\n\n" +
               "Please answer clearly and simply. Use examples if helpful. Be encouraging!";
    }

    /**
     * Make the actual API call to Claude.
     */
    private String callClaudeApi(String prompt) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl("https://api.anthropic.com")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("x-api-key", apiKey)
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .build();

            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", 1024,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            // Make API call
            String responseJson = client.post()
                    .uri("/v1/messages")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response
            JsonNode responseNode = objectMapper.readTree(responseJson);
            return responseNode.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            return "⚠️ AI service temporarily unavailable. Error: " + e.getMessage() +
                   "\n\nPlease check your API key in application.properties.";
        }
    }

    /**
     * Fallback explanation when API key is not configured.
     * Uses templates to generate a reasonable explanation.
     */
    private String getFallbackExplanation(String code, String language) {
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 AI ASSISTANT EXPLANATION\n");
        sb.append("============================\n");
        sb.append("(Note: Configure your API key in application.properties for full AI explanations)\n\n");

        sb.append("📋 CODE OVERVIEW:\n");
        sb.append("This is a ").append(language).append(" program.\n\n");

        int lines = code.split("\n").length;
        sb.append("📊 QUICK STATS:\n");
        sb.append("• Total lines: ").append(lines).append("\n");
        sb.append("• Language: ").append(language).append("\n\n");

        if (language.equalsIgnoreCase("java")) {
            sb.append("💡 KEY CONCEPTS IN THIS JAVA CODE:\n");
            if (code.contains("class ")) sb.append("• Uses classes (Object-Oriented Programming)\n");
            if (code.contains("main")) sb.append("• Has a main method (program entry point)\n");
            if (code.contains("Scanner")) sb.append("• Uses Scanner to read user input\n");
            if (code.contains("System.out")) sb.append("• Uses System.out to display output\n");
            if (code.contains("if")) sb.append("• Has conditional logic (if statements)\n");
            if (code.contains("for") || code.contains("while")) sb.append("• Uses loops to repeat operations\n");
            if (code.contains("try")) sb.append("• Handles exceptions (error handling)\n");
        } else if (language.equalsIgnoreCase("python")) {
            sb.append("💡 KEY CONCEPTS IN THIS PYTHON CODE:\n");
            if (code.contains("def ")) sb.append("• Defines functions with 'def'\n");
            if (code.contains("class ")) sb.append("• Uses classes (OOP)\n");
            if (code.contains("print")) sb.append("• Uses print() for output\n");
            if (code.contains("input")) sb.append("• Takes user input\n");
            if (code.contains("import")) sb.append("• Imports external libraries\n");
        } else if (language.equalsIgnoreCase("c")) {
            sb.append("💡 KEY CONCEPTS IN THIS C CODE:\n");
            if (code.contains("#include")) sb.append("• Includes standard libraries\n");
            if (code.contains("main")) sb.append("• Has main() function (entry point)\n");
            if (code.contains("printf")) sb.append("• Uses printf() for output\n");
            if (code.contains("scanf")) sb.append("• Uses scanf() for input\n");
            if (code.contains("*")) sb.append("• May use pointers\n");
        }

        sb.append("\n🎯 TO GET FULL AI EXPLANATIONS:\n");
        sb.append("1. Get an API key from https://console.anthropic.com\n");
        sb.append("2. Add it to application.properties: app.ai.api.key=your_key\n");
        sb.append("3. Restart the application\n");

        return sb.toString();
    }

    /**
     * Fallback answer for specific questions.
     */
    private String getFallbackAnswer(String question, String code, String language) {
        return "🤖 AI Assistant Answer\n" +
               "======================\n" +
               "Your question: \"" + question + "\"\n\n" +
               "To get intelligent AI answers, please configure your Claude API key in application.properties.\n\n" +
               "In the meantime, here are some helpful resources:\n" +
               "• For " + language + " documentation: search '" + language + " official docs'\n" +
               "• For tutorials: YouTube has excellent " + language + " beginner courses\n" +
               "• For community help: Stack Overflow is a great resource\n\n" +
               "Steps to enable AI:\n" +
               "1. Visit https://console.anthropic.com\n" +
               "2. Create a free account and get your API key\n" +
               "3. Open src/main/resources/application.properties\n" +
               "4. Replace 'YOUR_ANTHROPIC_API_KEY_HERE' with your actual key\n" +
               "5. Restart the Spring Boot application";
    }
}
