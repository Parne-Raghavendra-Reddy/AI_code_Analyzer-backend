package com.codeanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * CodeExecutionService - Compiles and runs user-submitted code.
 *
 * SECURITY MEASURES:
 * 1. Timeout: Code is killed after 10 seconds (prevent infinite loops)
 * 2. Temp files: Code saved to temp dir, cleaned up after execution
 * 3. Blocked commands: Dangerous system calls are filtered
 * 4. Sandboxing: Each run gets a unique temp directory
 *
 * Supported languages: Java, C, Python
 */
@Service
public class CodeExecutionService {

    @Value("${app.execution.timeout:10}")
    private int executionTimeout;

    @Value("${app.execution.max-output-length:10000}")
    private int maxOutputLength;

    // List of dangerous commands/patterns to block
    private static final List<String> BLOCKED_PATTERNS = Arrays.asList(
            "Runtime.exec", "ProcessBuilder", "System.exit",
            "rm -rf", "del /f", "format c:",
            "__import__('os').system", "subprocess.call",
            "/etc/passwd", "cmd.exe"
    );

    /**
     * Result of code execution.
     */
    public static class ExecutionResult {
        public String output;
        public String errorOutput;
        public boolean success;

        public ExecutionResult(String output, String errorOutput, boolean success) {
            this.output = output;
            this.errorOutput = errorOutput;
            this.success = success;
        }
    }

    /**
     * Main method - execute code based on language.
     *
     * @param language - "Java", "C", or "Python"
     * @param code     - the code to execute
     * @return ExecutionResult with output or errors
     */
    public ExecutionResult executeCode(String language, String code, String stdin) {

        // Security check: block dangerous code patterns
        String securityCheck = checkForDangerousCode(code);
        if (securityCheck != null) {
            return new ExecutionResult("", "Security violation: " + securityCheck, false);
        }

        switch (language.toLowerCase()) {
            case "java":
                return executeJava(code, stdin);
            case "c":
                return executeC(code, stdin);
            case "python":
                return executePython(code, stdin);
            default:
                return new ExecutionResult("", "Unsupported language: " + language, false);
        }
    }

    /**
     * Check if code contains dangerous patterns.
     * @return null if safe, or description of the violation
     */
    private String checkForDangerousCode(String code) {
        String lowerCode = code.toLowerCase();
        for (String pattern : BLOCKED_PATTERNS) {
            if (lowerCode.contains(pattern.toLowerCase())) {
                return "Code contains blocked pattern: '" + pattern + "'";
            }
        }
        return null; // Code is safe
    }

    /**
     * Execute Java code.
     * Steps: Save → Compile with javac → Run with java
     */
    private ExecutionResult executeJava(String code, String stdin) {
        // Create a unique temp directory for this execution
        String tempDirPath = System.getProperty("java.io.tmpdir") + "/code_" + UUID.randomUUID();
        File tempDir = new File(tempDirPath);
        tempDir.mkdirs();

        try {
            // Extract class name from code (must match filename for Java)
            String className = extractJavaClassName(code);
            if (className == null) {
                className = "Main"; // Default class name
            }

            // Save code to .java file
            File javaFile = new File(tempDir, className + ".java");
            Files.writeString(javaFile.toPath(), code);

            // Step 1: Compile the Java code
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "javac", javaFile.getAbsolutePath()
            );
            compileBuilder.directory(tempDir);
            compileBuilder.redirectErrorStream(false);

            ExecutionResult compileResult = runProcess(compileBuilder, "Compilation", null);
            if (!compileResult.success) {
                return compileResult; // Return compilation errors
            }

            // Step 2: Run the compiled Java program
            ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", tempDirPath, className);
            runBuilder.directory(tempDir);

            return runProcess(runBuilder, "Execution", stdin);

        } catch (Exception e) {
            return new ExecutionResult("", "Error setting up Java execution: " + e.getMessage(), false);
        } finally {
            // Clean up temp files
            deleteDirectory(tempDir);
        }
    }

    /**
     * Execute C code.
     * Steps: Save → Compile with gcc → Run executable
     */
    private ExecutionResult executeC(String code, String stdin) {
        String tempDirPath = System.getProperty("java.io.tmpdir") + "/code_" + UUID.randomUUID();
        File tempDir = new File(tempDirPath);
        tempDir.mkdirs();

        try {
            // Save code to .c file
            File cFile = new File(tempDir, "program.c");
            Files.writeString(cFile.toPath(), code);

            // Output executable path
            File outputFile = new File(tempDir, "program");

            // Step 1: Compile with gcc
            ProcessBuilder compileBuilder = new ProcessBuilder(
                    "gcc", cFile.getAbsolutePath(), "-o", outputFile.getAbsolutePath()
            );
            compileBuilder.directory(tempDir);

            ExecutionResult compileResult = runProcess(compileBuilder, "Compilation", null);
            if (!compileResult.success) {
                return compileResult;
            }

            // Step 2: Run the compiled program
            ProcessBuilder runBuilder = new ProcessBuilder(outputFile.getAbsolutePath());
            runBuilder.directory(tempDir);

            return runProcess(runBuilder, "Execution", stdin);

        } catch (Exception e) {
            return new ExecutionResult("", "Error setting up C execution: " + e.getMessage(), false);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    /**
     * Execute Python code.
     * Python is interpreted - just run with python3/python
     */
    private ExecutionResult executePython(String code, String stdin) {
        String tempDirPath = System.getProperty("java.io.tmpdir") + "/code_" + UUID.randomUUID();
        File tempDir = new File(tempDirPath);
        tempDir.mkdirs();

        try {
            // Save code to .py file
            File pyFile = new File(tempDir, "program.py");
            Files.writeString(pyFile.toPath(), code);

            // Try python3 first, fallback to python
            ProcessBuilder runBuilder;
            if (isPythonAvailable("python3")) {
                runBuilder = new ProcessBuilder("python3", pyFile.getAbsolutePath());
            } else {
                runBuilder = new ProcessBuilder("python", pyFile.getAbsolutePath());
            }
            runBuilder.directory(tempDir);

            return runProcess(runBuilder, "Execution", stdin);

        } catch (Exception e) {
            return new ExecutionResult("", "Error setting up Python execution: " + e.getMessage(), false);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    /**
     * Run a process with timeout and capture output.
     */
    private ExecutionResult runProcess(ProcessBuilder builder, String phase, String stdin) {
        try {
            builder.redirectErrorStream(false);
            Process process = builder.start();

            if (stdin != null && !stdin.trim().isEmpty()) {
                try (OutputStream os = process.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                    writer.write(stdin);
                    writer.flush();
                }
            }

            // Capture output and error in separate threads (avoid deadlock)
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<String> outputFuture = executor.submit(() -> readStream(process.getInputStream()));
            Future<String> errorFuture = executor.submit(() -> readStream(process.getErrorStream()));

            // Wait for process with timeout (prevents infinite loops)
            boolean finished = process.waitFor(executionTimeout, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                executor.shutdownNow();
                return new ExecutionResult("",
                        "Error: Code execution timed out after " + executionTimeout + " seconds.\n" +
                        "Possible reasons: infinite loop, waiting for input, or very slow code.", false);
            }

            String output = outputFuture.get(2, TimeUnit.SECONDS);
            String errorOutput = errorFuture.get(2, TimeUnit.SECONDS);
            executor.shutdown();

            int exitCode = process.exitValue();
            boolean success = (exitCode == 0);

            // Truncate very long output
            if (output != null && output.length() > maxOutputLength) {
                output = output.substring(0, maxOutputLength) + "\n... [Output truncated]";
            }

            return new ExecutionResult(
                    output != null ? output : "",
                    errorOutput != null ? errorOutput : "",
                    success && (errorOutput == null || errorOutput.isBlank())
            );

        } catch (Exception e) {
            return new ExecutionResult("", phase + " failed: " + e.getMessage(), false);
        }
    }

    /** Read all content from an input stream */
    private String readStream(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }

    /** Extract the public class name from Java code */
    private String extractJavaClassName(String code) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "public\\s+class\\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Main";
    }

    /** Check if python is available on the system */
    private boolean isPythonAvailable(String command) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{command, "--version"});
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Recursively delete a directory and all its contents */
    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}
