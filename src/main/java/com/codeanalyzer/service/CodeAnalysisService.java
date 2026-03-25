package com.codeanalyzer.service;

import com.codeanalyzer.dto.Dtos.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

/**
 * CodeAnalysisService - Analyzes code structure and generates explanations.
 *
 * This service:
 * 1. Calculates code metrics (lines, complexity, methods, variables)
 * 2. Generates beginner-friendly step-by-step explanations
 * 3. Explains the workflow (how output is produced)
 * 4. Suggests YouTube videos based on concepts detected
 */
@Service
public class CodeAnalysisService {

    // =============================================
    // METRICS CALCULATION
    // =============================================

    /** Count total lines of code (excluding empty lines and comments) */
    public int countLines(String code) {
        if (code == null || code.isEmpty()) return 0;
        String[] lines = code.split("\n");
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            // Count non-empty, non-comment lines
            if (!trimmed.isEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("*")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate Cyclomatic Complexity.
     *
     * Cyclomatic complexity measures how complex code is.
     * It counts decision points: if, else, for, while, case, catch, &&, ||
     * - Score 1-5: Simple code (easy to understand)
     * - Score 6-10: Moderate complexity
     * - Score 11+: Complex (consider simplifying)
     */
    public int calculateCyclomaticComplexity(String code) {
        int complexity = 1; // Start at 1 (base path through code)

        // Each decision point adds 1 to complexity
        String[] decisionKeywords = {"if", "else if", "for", "while", "case", "catch", "&&", "||", "?", "do"};

        for (String keyword : decisionKeywords) {
            // Count occurrences using pattern matching
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            Matcher matcher = pattern.matcher(code);
            while (matcher.find()) {
                complexity++;
            }
        }
        return complexity;
    }

    /** Count number of methods/functions in the code */
    public int countMethods(String code, String language) {
        int count = 0;

        if (language.equalsIgnoreCase("java")) {
            // Java method pattern: visibility returnType methodName(params) {
            Pattern pattern = Pattern.compile(
                    "(public|private|protected|static)?\\s+\\w+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{");
            Matcher matcher = pattern.matcher(code);
            while (matcher.find()) {
                // Exclude if/for/while which also have parentheses
                String match = matcher.group();
                if (!match.trim().startsWith("if") && !match.trim().startsWith("for")
                        && !match.trim().startsWith("while") && !match.trim().startsWith("catch")) {
                    count++;
                }
            }
        } else if (language.equalsIgnoreCase("python")) {
            // Python function pattern: def functionName(params):
            Pattern pattern = Pattern.compile("def\\s+(\\w+)\\s*\\(");
            Matcher matcher = pattern.matcher(code);
            while (matcher.find()) {
                count++;
            }
        } else if (language.equalsIgnoreCase("c")) {
            // C function pattern: returnType functionName(params) {
            Pattern pattern = Pattern.compile("\\w+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{");
            Matcher matcher = pattern.matcher(code);
            while (matcher.find()) {
                String match = matcher.group();
                if (!match.contains("if") && !match.contains("for") && !match.contains("while")) {
                    count++;
                }
            }
        }

        return Math.max(count, 0);
    }

    /** Count variables declared in the code */
    public int countVariables(String code, String language) {
        int count = 0;

        if (language.equalsIgnoreCase("java")) {
            // Match: int x, String name, double price, etc.
            String[] javaTypes = {"int", "String", "double", "float", "long", "boolean",
                                  "char", "byte", "short", "Integer", "Double", "List",
                                  "ArrayList", "Map", "HashMap", "Scanner"};
            for (String type : javaTypes) {
                Pattern pattern = Pattern.compile("\\b" + type + "\\b\\s+\\w+\\s*[=;,\\)]");
                Matcher matcher = pattern.matcher(code);
                while (matcher.find()) {
                    count++;
                }
            }
        } else if (language.equalsIgnoreCase("python")) {
            // Python: variable = value (simple assignment)
            Pattern pattern = Pattern.compile("^\\s*(\\w+)\\s*=\\s*[^=]", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(code);
            while (matcher.find()) {
                // Skip keywords
                String varName = matcher.group(1);
                if (!varName.equals("if") && !varName.equals("for") && !varName.equals("return")) {
                    count++;
                }
            }
        } else if (language.equalsIgnoreCase("c")) {
            String[] cTypes = {"int", "double", "float", "char", "long", "short", "string"};
            for (String type : cTypes) {
                Pattern pattern = Pattern.compile("\\b" + type + "\\b\\s+\\w+\\s*[=;,\\)]");
                Matcher matcher = pattern.matcher(code);
                while (matcher.find()) {
                    count++;
                }
            }
        }

        return Math.max(count, 0);
    }

    /** Count constructors in Java code */
    public int countConstructors(String code, String language) {
        if (!language.equalsIgnoreCase("java")) return 0;

        int count = 0;
        // Extract class name first
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(code);

        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            // Constructor: ClassName(params) {
            Pattern constructorPattern = Pattern.compile(
                    "(public|private|protected)?\\s*" + className + "\\s*\\([^)]*\\)\\s*\\{");
            Matcher constructorMatcher = constructorPattern.matcher(code);
            while (constructorMatcher.find()) {
                count++;
            }
        }
        return count;
    }

    // =============================================
    // BEGINNER EXPLANATION GENERATION
    // =============================================

    /**
     * Generate a beginner-friendly step-by-step explanation of the code.
     *
     * This is the most educational feature - it breaks down what the code does
     * in simple English, like a teacher explaining to a student.
     */
    public String generateBeginnerExplanation(String code, String language, String output) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("📚 BEGINNER'S GUIDE - What This Code Does\n");
        explanation.append("==========================================\n\n");

        String[] lines = code.split("\n");
        int stepNumber = 1;

        if (language.equalsIgnoreCase("java")) {
            explanation.append(generateJavaExplanation(code, lines, stepNumber));
        } else if (language.equalsIgnoreCase("python")) {
            explanation.append(generatePythonExplanation(code, lines, stepNumber));
        } else if (language.equalsIgnoreCase("c")) {
            explanation.append(generateCExplanation(code, lines, stepNumber));
        }

        // Add what the output means
        if (output != null && !output.isBlank()) {
            explanation.append("\n📤 OUTPUT EXPLANATION:\n");
            explanation.append("------------------------\n");
            explanation.append("When you run this code, it produces:\n");
            explanation.append("  → ").append(output.trim()).append("\n");
        }

        return explanation.toString();
    }

    private String generateJavaExplanation(String code, String[] lines, int step) {
        StringBuilder sb = new StringBuilder();

        // Check for common Java patterns and explain them
        if (code.contains("public static void main")) {
            sb.append("Step ").append(step++).append(": 🚀 PROGRAM START\n");
            sb.append("   • Execution Engine: The program begins execution from the 'main' method.\n");
            sb.append("   • 'public': Anyone can access this method (required by the JVM to start it).\n");
            sb.append("   • 'static': The method belongs to the class itself, so no object needs to be created to run it.\n");
            sb.append("   • 'void': This method does not return a final value when it finishes.\n");
            sb.append("   • 'String[] args': This allows the program to accept command-line arguments.\n\n");
        }

        if (code.contains("Scanner")) {
            sb.append("Step ").append(step++).append(": 📥 INPUT SETUP\n");
            sb.append("   • A 'Scanner' object is instantiated to read input from the user.\n");
            sb.append("   • Think of Scanner as a listening device connected to your keyboard (System.in).\n");
            sb.append("   • It actively waits, pausing your program until the user types something and hits Enter.\n\n");
        }

        // Explain variable declarations
        Pattern varPattern = Pattern.compile("(int|String|double|float|boolean)\\s+(\\w+)\\s*=\\s*(.+);");
        Matcher varMatcher = varPattern.matcher(code);
        while (varMatcher.find()) {
            sb.append("Step ").append(step++).append(": 📦 VARIABLE CREATED\n");
            sb.append("   Type: ").append(varMatcher.group(1)).append("\n");
            sb.append("   Name: '").append(varMatcher.group(2)).append("'\n");
            sb.append("   Value: ").append(varMatcher.group(3).trim()).append("\n");
            sb.append("   ➡ Think of '").append(varMatcher.group(2)).append("' as a box that stores a ")
              .append(varMatcher.group(1)).append(" value.\n\n");
        }

        if (code.contains("if (") || code.contains("if(")) {
            sb.append("Step ").append(step++).append(": 🔀 DECISION POINT (if-statement)\n");
            sb.append("   • The program evaluates a strict boolean condition here.\n");
            sb.append("   • If the condition is TRUE → The code block inside the { curly braces } will be executed.\n");
            sb.append("   • If the condition is FALSE → The program completely ignores that block and jumps to the next section or 'else' block.\n\n");
        }

        if (code.contains("for (") || code.contains("for(")) {
            sb.append("Step ").append(step++).append(": 🔄 ITERATION LOOP (for-loop)\n");
            sb.append("   • A 'for' loop is designed to repeat a specific block of code a known number of times.\n");
            sb.append("   • Initialization: It sets a starting variable (e.g., int i = 0).\n");
            sb.append("   • Condition: It checks if it should run again (e.g., i < 10).\n");
            sb.append("   • Increment: It updates the variable after every pass (e.g., i++ adds 1 to i).\n\n");
        }

        if (code.contains("while (") || code.contains("while(")) {
            sb.append("Step ").append(step++).append(": 🔄 CONDITIONAL LOOP (while-loop)\n");
            sb.append("   • A 'while' loop keeps running back to the top over and over as long as the condition remains true.\n");
            sb.append("   • Danger: If the condition is never modified to become false inside the loop, the program will freeze in an infinite loop!\n\n");
        }

        if (code.contains("System.out.println") || code.contains("System.out.print")) {
            sb.append("Step ").append(step++).append(": 📤 TERMINAL OUTPUT\n");
            sb.append("   • 'System.out.println()' tells the Java interpreter to push text to the standard output terminal.\n");
            sb.append("   • The 'ln' at the end means 'Line New' - it automatically presses Enter/Return after printing your text.\n");
            sb.append("   • We capture this exact stream here to show you the Execution Results.\n\n");
        }

        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(code);
        if (classMatcher.find()) {
            sb.append("🏗 CLASS STRUCTURE:\n");
            sb.append("   • Class Identifier: '").append(classMatcher.group(1)).append("'\n");
            sb.append("   • In Java, everything must live inside a Class. It acts as an architectural blueprint.\n");
            sb.append("   • Memory Allocation: The class itself doesn't do anything until you instantiate it into an Object (unless using static methods like main!).\n\n");
        }

        // Explain methods found
        Pattern methodPattern = Pattern.compile("(public|private|protected)\\s+(\\w+)\\s+(\\w+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(code);
        boolean firstMethod = true;
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(3);
            String returnType = methodMatcher.group(2);
            if (!methodName.equals("main")) {
                if (firstMethod) {
                    sb.append("🔧 METHODS IN THIS CODE:\n");
                    firstMethod = false;
                }
                sb.append("   • Method '").append(methodName).append("':\n");
                sb.append("     Returns: ").append(returnType)
                  .append(returnType.equals("void") ? " (nothing)" : "").append("\n");
                sb.append("     Purpose: Performs a specific task when called.\n");
            }
        }
        if (!firstMethod) sb.append("\n");

        return sb.toString();
    }

    private String generatePythonExplanation(String code, String[] lines, int step) {
        StringBuilder sb = new StringBuilder();

        if (code.contains("def ")) {
            sb.append("Step ").append(step++).append(": 🔧 FUNCTION DEFINITIONS\n");
            sb.append("   Functions in Python start with 'def'.\n");
            sb.append("   They are reusable blocks of code.\n\n");
        }

        if (code.contains("print(")) {
            sb.append("Step ").append(step++).append(": 📤 OUTPUT\n");
            sb.append("   'print()' displays text on the screen.\n\n");
        }

        if (code.contains("input(")) {
            sb.append("Step ").append(step++).append(": 📥 USER INPUT\n");
            sb.append("   'input()' waits for the user to type something and press Enter.\n\n");
        }

        if (code.contains("for ") && code.contains(" in ")) {
            sb.append("Step ").append(step++).append(": 🔄 FOR LOOP\n");
            sb.append("   Python's for loop goes through each item in a collection.\n");
            sb.append("   Example: 'for x in [1,2,3]' visits x=1, then x=2, then x=3.\n\n");
        }

        if (code.contains("if ") || code.contains("elif ") || code.contains("else:")) {
            sb.append("Step ").append(step++).append(": 🔀 CONDITIONS\n");
            sb.append("   if/elif/else checks conditions and runs code accordingly.\n\n");
        }

        // Explain variable assignments
        Pattern varPattern = Pattern.compile("^(\\w+)\\s*=\\s*(.+)$", Pattern.MULTILINE);
        Matcher varMatcher = varPattern.matcher(code);
        while (varMatcher.find()) {
            String varName = varMatcher.group(1);
            if (!varName.equals("def") && !varName.startsWith("#")) {
                sb.append("Step ").append(step++).append(": 📦 VARIABLE: '").append(varName).append("'\n");
                sb.append("   Value: ").append(varMatcher.group(2).trim()).append("\n");
                sb.append("   ➡ '").append(varName).append("' stores this value for later use.\n\n");
            }
        }

        return sb.toString();
    }

    private String generateCExplanation(String code, String[] lines, int step) {
        StringBuilder sb = new StringBuilder();

        if (code.contains("#include")) {
            sb.append("Step ").append(step++).append(": 📚 HEADER FILES\n");
            sb.append("   #include adds pre-built functions to your program.\n");
            sb.append("   #include <stdio.h> gives you printf() and scanf().\n\n");
        }

        if (code.contains("int main()") || code.contains("int main(void)")) {
            sb.append("Step ").append(step++).append(": 🚀 PROGRAM START\n");
            sb.append("   'int main()' is where every C program begins.\n");
            sb.append("   It returns an int: 0 means success, anything else means error.\n\n");
        }

        if (code.contains("printf")) {
            sb.append("Step ").append(step++).append(": 📤 OUTPUT\n");
            sb.append("   'printf()' prints formatted text to the screen.\n");
            sb.append("   %d = integer, %f = float, %s = string, \\n = new line\n\n");
        }

        if (code.contains("scanf")) {
            sb.append("Step ").append(step++).append(": 📥 USER INPUT\n");
            sb.append("   'scanf()' reads input from the user.\n");
            sb.append("   The & symbol means 'address of' - it tells scanf where to store the value.\n\n");
        }

        if (code.contains("for (") || code.contains("for(")) {
            sb.append("Step ").append(step++).append(": 🔄 FOR LOOP\n");
            sb.append("   A for loop repeats code a specific number of times.\n\n");
        }

        if (code.contains("return 0")) {
            sb.append("Step ").append(step++).append(": 🏁 PROGRAM END\n");
            sb.append("   'return 0' signals that the program finished successfully.\n\n");
        }

        return sb.toString();
    }

    // =============================================
    // WORKFLOW EXPLANATION
    // =============================================

    /**
     * Explain how the output was produced from the code.
     * This traces the logic that led to the output.
     */
    public String generateWorkflowExplanation(String code, String language, String output) {
        StringBuilder workflow = new StringBuilder();
        workflow.append("⚙️ WORKFLOW - How Output Was Produced\n");
        workflow.append("======================================\n\n");

        if (output == null || output.isBlank()) {
            workflow.append("No output was produced by this code.\n");
            return workflow.toString();
        }

        workflow.append("The code produced this output:\n");
        workflow.append("  → ").append(output.trim()).append("\n\n");
        workflow.append("Here's how it was calculated:\n\n");

        // Try to detect arithmetic operations and trace them
        detectArithmeticWorkflow(code, workflow);

        // Detect string concatenation
        if (code.contains("+") && (code.contains("\"") || code.contains("'"))) {
            workflow.append("📝 String Operation Detected:\n");
            workflow.append("   • The code dynamically built text using the '+' operator.\n");
            workflow.append("   • It joined literal strings (like \"Hello\") with variables in memory.\n");
            workflow.append("   • This allows the program to construct customized output messages rather than just printing raw data.\n\n");
        }

        // Detect method calls
        if (language.equalsIgnoreCase("java")) {
            Pattern methodCallPattern = Pattern.compile("(\\w+)\\.(\\w+)\\(");
            Matcher matcher = methodCallPattern.matcher(code);
            Set<String> foundCalls = new HashSet<>();
            while (matcher.find()) {
                String call = matcher.group(1) + "." + matcher.group(2) + "()";
                if (foundCalls.add(call)) {
                    workflow.append("📞 Sub-Routine Invocation: `").append(call).append("`\n");
                    workflow.append("   • During execution, the main flow paused to jump into this sub-routine.\n");
                    workflow.append("   • The sub-routine executed its own isolate block of logic (and likely modified the output or state).\n");
                    workflow.append("   • Once finished, execution control was handed back to the caller.\n\n");
                }
            }
        }

        // Detect loops and their role in output
        if (code.contains("for") || code.contains("while")) {
            workflow.append("🔄 Cyclic Flow Contribution:\n");
            workflow.append("   • We detected a loop structure driving this workflow.\n");
            workflow.append("   • Instead of writing the same operation multiple times, the CPU was instructed to cycle through a memory block.\n");
            workflow.append("   • During each cycle, variables were mutated, conditions were re-evaluated, and a portion of the final output was generated iteratively.\n\n");
        }

        // Detect conditional branching (specifically catching Even/Odd logic)
        if (code.contains("if") && code.contains("% 2 == 0")) {
            workflow.append("🔀 Even/Odd Logic Branch:\n");
            workflow.append("   • We used an 'if' statement with a modulo operator (%) to check if the variable is even or odd.\n");
            workflow.append("   • Because the condition evaluated to ").append(output.trim().equals("Even") ? "TRUE" : "FALSE").append(", the program executed the '").append(output.trim()).append("' branch.\n\n");
        } else if (code.contains("if")) {
            workflow.append("🔀 Conditional Branching:\n");
            workflow.append("   • We used an 'if' statement to evaluate a specific condition.\n");
            workflow.append("   • Depending on whether that condition was TRUE or FALSE, the execution flow jumped into a specific block of code to produce this output.\n\n");
        }

        workflow.append("💡 Execution Summary:\n");
        workflow.append("   • The OS allocated memory and gave control to the program's main function.\n");
        workflow.append("   • The processor evaluated the code strictly top-to-bottom, jumping only when instructed by loops or decision branches.\n");
        workflow.append("   • Variables acted as living registers holding data, culminating in the standard output stream you see above.\n");

        return workflow.toString();
    }

    private void detectArithmeticWorkflow(String code, StringBuilder workflow) {
        // Look for common arithmetic patterns
        Pattern addPattern = Pattern.compile("(\\w+)\\s*\\+\\s*(\\w+)");
        Matcher addMatcher = addPattern.matcher(code);

        boolean foundArithmetic = false;
        Set<String> found = new HashSet<>();

        while (addMatcher.find()) {
            String expr = addMatcher.group(0);
            if (!found.contains(expr) && !expr.contains("++")) {
                if (!foundArithmetic) {
                    workflow.append("➕ Arithmetic Synthesis (Addition):\n");
                    workflow.append("   • The ALU (Arithmetic Logic Unit) took multiple values in memory and combined them.\n");
                    foundArithmetic = true;
                }
                workflow.append("   • Expression calculated: `").append(expr).append("`\n");
                found.add(expr);
            }
        }
        if (foundArithmetic) workflow.append("\n");

        // Subtraction
        Pattern subPattern = Pattern.compile("(\\w+)\\s*-\\s*(\\w+)");
        Matcher subMatcher = subPattern.matcher(code);
        boolean foundSub = false;
        while (subMatcher.find()) {
            String expr = subMatcher.group(0);
            if (!found.contains(expr) && !expr.contains("->")) {
                if (!foundSub) {
                    workflow.append("➖ Subtraction Operations Found:\n");
                    foundSub = true;
                }
                workflow.append("   • ").append(expr).append("\n");
                found.add(expr);
            }
        }
        if (foundSub) workflow.append("\n");

        // Multiplication
        if (code.contains("*")) {
            workflow.append("✖️ Multiplication detected in code.\n\n");
        }

        // Division
        if (code.contains("/")) {
            workflow.append("➗ Division detected in code.\n\n");
        }
    }

    // =============================================
    // VIDEO SUGGESTIONS
    // =============================================

    /**
     * Suggest relevant YouTube tutorial videos based on code concepts.
     */
    public List<VideoSuggestion> suggestVideos(String code, String language) {
        List<VideoSuggestion> suggestions = new ArrayList<>();

        if (language.equalsIgnoreCase("java")) {
            suggestions.add(new VideoSuggestion(
                "Java Tutorial for Beginners",
                "https://www.youtube.com/results?search_query=Java+tutorial+for+beginners",
                "Complete Java programming basics"
            ));

            if (code.contains("Scanner")) {
                suggestions.add(new VideoSuggestion(
                    "Java Scanner Class Tutorial",
                    "https://www.youtube.com/results?search_query=Java+Scanner+class+tutorial",
                    "Learn how to take user input in Java"
                ));
            }
            if (code.contains("ArrayList") || code.contains("List")) {
                suggestions.add(new VideoSuggestion(
                    "Java ArrayList Tutorial",
                    "https://www.youtube.com/results?search_query=Java+ArrayList+tutorial",
                    "Working with dynamic arrays in Java"
                ));
            }
            if (code.contains("for") || code.contains("while")) {
                suggestions.add(new VideoSuggestion(
                    "Java Loops Tutorial",
                    "https://www.youtube.com/results?search_query=Java+loops+for+while+tutorial",
                    "Master loops in Java"
                ));
            }
            if (code.contains("class ") && code.contains("extends")) {
                suggestions.add(new VideoSuggestion(
                    "Java Inheritance Tutorial",
                    "https://www.youtube.com/results?search_query=Java+inheritance+OOP+tutorial",
                    "Object-Oriented Programming concepts"
                ));
            }
            if (code.contains("interface ")) {
                suggestions.add(new VideoSuggestion(
                    "Java Interfaces Tutorial",
                    "https://www.youtube.com/results?search_query=Java+interfaces+tutorial",
                    "Learn interfaces in Java"
                ));
            }
            if (code.contains("try") && code.contains("catch")) {
                suggestions.add(new VideoSuggestion(
                    "Java Exception Handling",
                    "https://www.youtube.com/results?search_query=Java+exception+handling+tutorial",
                    "Handle errors gracefully in Java"
                ));
            }
        } else if (language.equalsIgnoreCase("python")) {
            suggestions.add(new VideoSuggestion(
                "Python Tutorial for Beginners",
                "https://www.youtube.com/results?search_query=Python+tutorial+for+beginners",
                "Complete Python programming basics"
            ));

            if (code.contains("def ")) {
                suggestions.add(new VideoSuggestion(
                    "Python Functions Tutorial",
                    "https://www.youtube.com/results?search_query=Python+functions+def+tutorial",
                    "Learn to write functions in Python"
                ));
            }
            if (code.contains("class ")) {
                suggestions.add(new VideoSuggestion(
                    "Python OOP Tutorial",
                    "https://www.youtube.com/results?search_query=Python+object+oriented+programming",
                    "Classes and Objects in Python"
                ));
            }
            if (code.contains("import ")) {
                suggestions.add(new VideoSuggestion(
                    "Python Libraries and Imports",
                    "https://www.youtube.com/results?search_query=Python+import+modules+tutorial",
                    "Using Python libraries and modules"
                ));
            }
        } else if (language.equalsIgnoreCase("c")) {
            suggestions.add(new VideoSuggestion(
                "C Programming Tutorial for Beginners",
                "https://www.youtube.com/results?search_query=C+programming+tutorial+beginners",
                "Complete C programming basics"
            ));

            if (code.contains("pointer") || code.contains("*")) {
                suggestions.add(new VideoSuggestion(
                    "C Pointers Tutorial",
                    "https://www.youtube.com/results?search_query=C+pointers+tutorial",
                    "Understanding pointers in C"
                ));
            }
            if (code.contains("struct")) {
                suggestions.add(new VideoSuggestion(
                    "C Structures Tutorial",
                    "https://www.youtube.com/results?search_query=C+struct+tutorial",
                    "Using structures in C"
                ));
            }
        }

        // Limit to 4 suggestions
        return suggestions.size() > 4 ? suggestions.subList(0, 4) : suggestions;
    }
}
