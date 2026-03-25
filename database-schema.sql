-- ============================================================
-- AI Code Analyzer - MySQL Database Schema
-- ============================================================
-- Run this SQL file in MySQL Workbench or MySQL CLI BEFORE
-- starting the Spring Boot application.
--
-- OR let Spring Boot auto-create tables by setting:
--   spring.jpa.hibernate.ddl-auto=update
-- (this is already set in application.properties)
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS code_analyzer_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Use the database
USE code_analyzer_db;

-- ============================================================
-- TABLE: users
-- Stores registered users with their roles
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,   -- BCrypt hashed password
    email       VARCHAR(100)    NOT NULL UNIQUE,
    role        VARCHAR(20)     NOT NULL DEFAULT 'USER',  -- USER or ADMIN
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: code_submissions
-- Stores every code submission with its analysis results
-- ============================================================
CREATE TABLE IF NOT EXISTS code_submissions (
    id                      BIGINT      NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT      NOT NULL,           -- FK to users.id
    language                VARCHAR(20) NOT NULL,           -- Java, C, Python
    code                    TEXT        NOT NULL,           -- The submitted code
    output                  TEXT,                           -- Execution output
    error_output            TEXT,                           -- Compilation/runtime errors
    execution_success       BOOLEAN     DEFAULT FALSE,      -- Was run successful?

    -- Code Analysis Metrics
    line_count              INT         DEFAULT 0,
    cyclomatic_complexity   INT         DEFAULT 0,
    method_count            INT         DEFAULT 0,
    variable_count          INT         DEFAULT 0,
    constructor_count       INT         DEFAULT 0,

    -- AI-Generated Explanations
    beginner_explanation    TEXT,
    workflow_explanation    TEXT,
    ai_explanation          TEXT,
    video_suggestions       TEXT,       -- JSON string of video list

    submitted_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_language (language),
    INDEX idx_submitted_at (submitted_at)
) ENGINE=InnoDB;

-- ============================================================
-- DEFAULT DATA: Insert admin and test user
-- (Spring Boot's DataInitializer does this automatically,
--  but this SQL is provided for manual setup too)
-- ============================================================

-- Default admin account (password: admin123)
-- Note: In real projects, NEVER store plain-text passwords!
-- The password here IS the BCrypt hash of "admin123"
INSERT IGNORE INTO users (username, email, password, role, is_active)
VALUES (
    'admin',
    'admin@codeanalyzer.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTpyIaWJ9lm',
    'ADMIN',
    TRUE
);

-- Default test user (password: user123)
INSERT IGNORE INTO users (username, email, password, role, is_active)
VALUES (
    'user',
    'user@codeanalyzer.com',
    '$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkLul3RpzH7YBMjBsRUH47JAG0aBa',
    'USER',
    TRUE
);

-- ============================================================
-- USEFUL QUERIES FOR ADMIN/DEBUGGING
-- ============================================================

-- See all users:
-- SELECT id, username, email, role, is_active, created_at FROM users;

-- See all submissions:
-- SELECT cs.id, u.username, cs.language, cs.execution_success, cs.line_count, cs.submitted_at
-- FROM code_submissions cs JOIN users u ON cs.user_id = u.id
-- ORDER BY cs.submitted_at DESC;

-- Count submissions per user:
-- SELECT u.username, COUNT(cs.id) as total_submissions
-- FROM users u LEFT JOIN code_submissions cs ON u.id = cs.user_id
-- GROUP BY u.id, u.username;

-- Success rate:
-- SELECT
--   COUNT(*) as total,
--   SUM(CASE WHEN execution_success = 1 THEN 1 ELSE 0 END) as successful,
--   SUM(CASE WHEN execution_success = 0 THEN 1 ELSE 0 END) as failed
-- FROM code_submissions;
