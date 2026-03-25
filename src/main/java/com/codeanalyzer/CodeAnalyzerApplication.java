package com.codeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the AI Code Analyzer application.
 * This class bootstraps the entire Spring Boot application.
 */
@SpringBootApplication
public class CodeAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAnalyzerApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  AI Code Analyzer is running!");
        System.out.println("  Open: http://localhost:8080");
        System.out.println("==============================================");
    }
}
