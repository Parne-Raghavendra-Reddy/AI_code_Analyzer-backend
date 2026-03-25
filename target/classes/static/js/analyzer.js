/**
 * analyzer.js - Main logic for the Code Analyzer page.
 *
 * Handles:
 * - Code editor (CodeMirror)
 * - Code submission and analysis
 * - Displaying results (output, metrics, explanations)
 * - AI chat functionality
 * - Video suggestions
 * - Sample code loading
 */

// ============================================================
// GLOBALS
// ============================================================

let editor;          // CodeMirror editor instance
let currentCode = ''; // Last submitted code
let currentLang = 'Java'; // Currently selected language
let currentSubmissionId = null; // Last submission ID (for AI chat context)

// ============================================================
// INIT
// ============================================================

window.addEventListener('load', () => {
    // Check authentication
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // Set username in navbar
    const username = localStorage.getItem('username');
    document.getElementById('navUsername').innerHTML =
        `<i class="fas fa-user-circle"></i> ${username || 'User'}`;

    // Show admin link if admin
    if (localStorage.getItem('userRole') === 'ADMIN') {
        document.getElementById('adminLink').style.display = 'flex';
    }

    // Initialize CodeMirror editor
    initEditor();

    // Load default sample code
    loadSample('hello');
});

/**
 * Initialize CodeMirror code editor with syntax highlighting.
 */
function initEditor() {
    editor = CodeMirror.fromTextArea(document.getElementById('codeEditor'), {
        mode: 'text/x-java',         // Default Java syntax
        theme: 'dracula',            // Dark theme
        lineNumbers: true,           // Show line numbers
        matchBrackets: true,         // Highlight matching brackets
        autoCloseBrackets: true,     // Auto close brackets
        indentWithTabs: false,       // Use spaces
        indentUnit: 4,               // 4 spaces indent
        tabSize: 4,
        lineWrapping: true,          // Wrap long lines
        extraKeys: {
            'Ctrl-Enter': analyzeCode,  // Ctrl+Enter to run
            'Cmd-Enter': analyzeCode    // Mac
        }
    });

    // Make editor fill container
    editor.setSize('100%', '100%');
}

/**
 * Called when user changes the language dropdown.
 * Updates CodeMirror syntax highlighting.
 */
function onLanguageChange() {
    const lang = document.getElementById('languageSelect').value;
    currentLang = lang;

    // Update editor syntax highlighting
    const modeMap = {
        'Java': 'text/x-java',
        'C': 'text/x-csrc',
        'Python': 'text/x-python'
    };
    editor.setOption('mode', modeMap[lang] || 'text/x-java');

    // Load default sample for new language
    loadSample('hello');
}

// ============================================================
// SAMPLE CODE
// ============================================================

/**
 * Sample code templates for each language.
 * These help users get started quickly.
 */
const samples = {
    Java: {
        hello: `public class Main {
    /**
     * This is the entry point of the Java program.
     * Every Java program must have a main method.
     */
    public static void main(String[] args) {
        // Print a greeting message to the screen
        System.out.println("Hello, World!");
        System.out.println("Welcome to AI Code Analyzer!");
    }
}`,
        calculator: `import java.util.Scanner;

public class Calculator {
    public static void main(String[] args) {
        // Variables to store numbers
        int a = 10;
        int b = 5;

        // Perform arithmetic operations
        int sum = a + b;
        int difference = a - b;
        int product = a * b;
        double quotient = (double) a / b;

        // Display results
        System.out.println("Calculator Results:");
        System.out.println("a = " + a + ", b = " + b);
        System.out.println("Sum: " + a + " + " + b + " = " + sum);
        System.out.println("Difference: " + a + " - " + b + " = " + difference);
        System.out.println("Product: " + a + " * " + b + " = " + product);
        System.out.println("Quotient: " + a + " / " + b + " = " + quotient);
    }
}`,
        loop: `public class LoopExample {
    public static void main(String[] args) {
        // For loop - prints numbers 1 to 5
        System.out.println("Counting with for loop:");
        for (int i = 1; i <= 5; i++) {
            System.out.println("Count: " + i);
        }

        // While loop - prints multiplication table of 3
        System.out.println("\\nMultiplication table of 3:");
        int multiplier = 1;
        while (multiplier <= 5) {
            System.out.println("3 x " + multiplier + " = " + (3 * multiplier));
            multiplier++;
        }
    }
}`,
        oop: `public class Animal {
    // Instance variables (fields)
    private String name;
    private int age;

    // Constructor - called when creating a new Animal
    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Method to make the animal speak
    public void speak() {
        System.out.println(name + " says: Hello!");
    }

    // Method to get animal info
    public String getInfo() {
        return "Name: " + name + ", Age: " + age;
    }

    public static void main(String[] args) {
        // Create Animal objects using the constructor
        Animal dog = new Animal("Buddy", 3);
        Animal cat = new Animal("Whiskers", 5);

        // Call methods on the objects
        dog.speak();
        cat.speak();
        System.out.println(dog.getInfo());
        System.out.println(cat.getInfo());
    }
}`
    },
    Python: {
        hello: `# This is a Python program
# Print a greeting message
print("Hello, World!")
print("Welcome to AI Code Analyzer!")

# Variables in Python
name = "Student"
print(f"Hello, {name}! Let's learn Python!")`,
        calculator: `# Python Calculator
# Variables to store numbers
a = 10
b = 5

# Arithmetic operations
sum_result = a + b
difference = a - b
product = a * b
quotient = a / b

# Display results
print("Calculator Results:")
print(f"a = {a}, b = {b}")
print(f"Sum: {a} + {b} = {sum_result}")
print(f"Difference: {a} - {b} = {difference}")
print(f"Product: {a} * {b} = {product}")
print(f"Quotient: {a} / {b} = {quotient}")`,
        loop: `# Python Loops Example

# For loop - prints numbers 1 to 5
print("Counting with for loop:")
for i in range(1, 6):
    print(f"Count: {i}")

# While loop
print("\\nCounting down:")
count = 5
while count > 0:
    print(f"Countdown: {count}")
    count -= 1
print("Done!")`,
        oop: `# Python OOP Example
class Animal:
    # Constructor - called when creating an Animal
    def __init__(self, name, age):
        self.name = name    # instance variable
        self.age = age      # instance variable

    # Method
    def speak(self):
        print(f"{self.name} says: Hello!")

    def get_info(self):
        return f"Name: {self.name}, Age: {self.age}"

# Create objects
dog = Animal("Buddy", 3)
cat = Animal("Whiskers", 5)

# Call methods
dog.speak()
cat.speak()
print(dog.get_info())
print(cat.get_info())`
    },
    C: {
        hello: `#include <stdio.h>

/* Main function - entry point of C program */
int main() {
    /* Print to screen using printf */
    printf("Hello, World!\\n");
    printf("Welcome to AI Code Analyzer!\\n");

    /* return 0 means program finished successfully */
    return 0;
}`,
        calculator: `#include <stdio.h>

int main() {
    /* Declare variables */
    int a = 10;
    int b = 5;
    int sum, difference, product;
    float quotient;

    /* Perform calculations */
    sum = a + b;
    difference = a - b;
    product = a * b;
    quotient = (float)a / b;

    /* Display results */
    printf("Calculator Results:\\n");
    printf("a = %d, b = %d\\n", a, b);
    printf("Sum: %d + %d = %d\\n", a, b, sum);
    printf("Difference: %d - %d = %d\\n", a, b, difference);
    printf("Product: %d * %d = %d\\n", a, b, product);
    printf("Quotient: %d / %d = %.2f\\n", a, b, quotient);

    return 0;
}`,
        loop: `#include <stdio.h>

int main() {
    int i;

    /* For loop - print 1 to 5 */
    printf("Counting with for loop:\\n");
    for (i = 1; i <= 5; i++) {
        printf("Count: %d\\n", i);
    }

    /* While loop */
    printf("\\nMultiplication table of 3:\\n");
    int multiplier = 1;
    while (multiplier <= 5) {
        printf("3 x %d = %d\\n", multiplier, 3 * multiplier);
        multiplier++;
    }

    return 0;
}`,
        oop: `#include <stdio.h>
#include <string.h>

/* Structure - like a class in C */
struct Animal {
    char name[50];
    int age;
};

/* Function to make animal speak */
void speak(struct Animal animal) {
    printf("%s says: Hello!\\n", animal.name);
}

/* Function to print animal info */
void printInfo(struct Animal animal) {
    printf("Name: %s, Age: %d\\n", animal.name, animal.age);
}

int main() {
    /* Create Animal variables (like objects) */
    struct Animal dog;
    struct Animal cat;

    /* Set values */
    strcpy(dog.name, "Buddy");
    dog.age = 3;
    strcpy(cat.name, "Whiskers");
    cat.age = 5;

    /* Call functions */
    speak(dog);
    speak(cat);
    printInfo(dog);
    printInfo(cat);

    return 0;
}`
    }
};

/**
 * Load a sample code into the editor.
 * @param type - 'hello', 'calculator', 'loop', or 'oop'
 */
function loadSample(type) {
    const lang = document.getElementById('languageSelect').value;
    const sampleCode = samples[lang] && samples[lang][type];
    if (sampleCode) {
        editor.setValue(sampleCode);
        editor.refresh();
    }
}

/**
 * Clear the code editor.
 */
function clearCode() {
    editor.setValue('');
    editor.focus();
}

// ============================================================
// MAIN: ANALYZE CODE
// ============================================================

/**
 * Submit code to the backend for analysis.
 * This is the main function called when user clicks "Run & Analyze".
 */
async function analyzeCode() {
    const code = editor.getValue().trim();
    const language = document.getElementById('languageSelect').value;

    if (!code) {
        alert('Please write some code first!');
        return;
    }

    // Save for AI chat context
    currentCode = code;
    currentLang = language;

    // Show loading state
    showLoading(true);

    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch('/api/code/analyze', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ language, code })
        });

        if (response.status === 401) {
            // Token expired
            logout();
            return;
        }

        const data = await response.json();

        if (response.ok) {
            currentSubmissionId = data.submissionId;
            displayResults(data);
        } else {
            showError('Analysis failed: ' + (data.message || 'Unknown error'));
        }

    } catch (err) {
        showError('Network error: ' + err.message + '. Is the server running?');
    } finally {
        showLoading(false);
    }
}

// ============================================================
// DISPLAY RESULTS
// ============================================================

/**
 * Display all analysis results in the right panel.
 */
function displayResults(data) {
    // Show results panel, hide welcome
    document.getElementById('welcomeState').style.display = 'none';
    document.getElementById('resultsTabs').style.display = 'block';

    // ---- OUTPUT TAB ----
    if (data.executionSuccess) {
        document.getElementById('outputSuccess').style.display = 'block';
        document.getElementById('outputError').style.display = 'none';
        document.getElementById('outputText').textContent = data.output || '(No output)';
    } else {
        document.getElementById('outputSuccess').style.display = 'none';
        document.getElementById('outputError').style.display = 'block';
        document.getElementById('errorText').textContent =
            data.errorOutput || data.output || 'Unknown error';
    }

    // ---- METRICS TAB ----
    document.getElementById('metricLines').textContent = data.lineCount;
    document.getElementById('metricComplexity').textContent = data.cyclomaticComplexity;
    document.getElementById('metricMethods').textContent = data.methodCount;
    document.getElementById('metricVariables').textContent = data.variableCount;
    document.getElementById('metricConstructors').textContent = data.constructorCount;

    // Complexity rating
    const cc = data.cyclomaticComplexity;
    let rating, color;
    if (cc <= 5) { rating = 'Simple ✅'; color = '#10B981'; }
    else if (cc <= 10) { rating = 'Moderate ⚠️'; color = '#F59E0B'; }
    else { rating = 'Complex 🔴'; color = '#EF4444'; }
    document.getElementById('metricRating').textContent = rating;
    document.getElementById('metricRating').style.color = color;

    // ---- EXPLANATION TAB ----
    document.getElementById('explanationText').textContent =
        data.beginnerExplanation || 'No explanation available.';

    // ---- WORKFLOW TAB ----
    document.getElementById('workflowText').textContent =
        data.workflowExplanation || 'No workflow explanation available.';

    // ---- AI CHAT TAB ----
    document.getElementById('aiInitialText').textContent =
        data.aiExplanation || 'Configure your API key for AI explanations!';

    // ---- VIDEOS TAB ----
    renderVideos(data.videoSuggestions || []);

    // Auto-show output tab
    showResultTab('output');
}

/**
 * Render video suggestion cards.
 */
function renderVideos(videos) {
    const container = document.getElementById('videosList');
    if (!videos || videos.length === 0) {
        container.innerHTML = '<p style="color:var(--text-muted)">No video suggestions available.</p>';
        return;
    }

    container.innerHTML = videos.map(v => `
        <a href="${v.url}" target="_blank" rel="noopener noreferrer" class="video-card">
            <div class="video-icon"><i class="fab fa-youtube"></i></div>
            <div class="video-info">
                <div class="video-title">${v.title}</div>
                <div class="video-desc">${v.description}</div>
            </div>
            <div class="video-arrow"><i class="fas fa-external-link-alt"></i></div>
        </a>
    `).join('');
}

// ============================================================
// AI CHAT
// ============================================================

/**
 * Send a question to the AI assistant.
 */
async function sendChat() {
    const input = document.getElementById('chatInput');
    const question = input.value.trim();
    if (!question) return;

    input.value = '';

    // Add user message to chat
    addChatMessage(question, 'user');

    // Add thinking indicator
    const thinkingId = 'thinking-' + Date.now();
    addChatMessage('Thinking...', 'bot', thinkingId);

    try {
        const token = localStorage.getItem('authToken');
        const response = await fetch('/api/code/ai-chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({
                question: question,
                code: currentCode,
                language: currentLang
            })
        });

        const data = await response.json();

        // Remove thinking indicator
        const thinking = document.getElementById(thinkingId);
        if (thinking) thinking.remove();

        addChatMessage(data.answer || 'No response from AI.', 'bot');

    } catch (err) {
        const thinking = document.getElementById(thinkingId);
        if (thinking) thinking.remove();
        addChatMessage('Error contacting AI: ' + err.message, 'bot');
    }
}

/**
 * Pre-fill a quick question into the chat.
 */
function askQuick(question) {
    document.getElementById('chatInput').value = question;
    sendChat();
}

/**
 * Add a message bubble to the chat UI.
 */
function addChatMessage(text, sender, id) {
    const messages = document.getElementById('chatMessages');
    const div = document.createElement('div');
    if (id) div.id = id;
    div.className = sender === 'user' ? 'chat-msg-user' : 'chat-msg-bot';
    div.textContent = text;
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}

// ============================================================
// TAB NAVIGATION
// ============================================================

function showResultTab(tabId) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(el => el.style.display = 'none');
    // Remove active from all tabs
    document.querySelectorAll('#resultsTabs .tab').forEach(el => el.classList.remove('active'));

    // Show selected tab
    const content = document.getElementById('tab-' + tabId);
    if (content) content.style.display = 'block';

    // Find and activate the clicked tab button
    const tabs = document.querySelectorAll('#resultsTabs .tab');
    tabs.forEach(tab => {
        if (tab.getAttribute('onclick').includes(tabId)) {
            tab.classList.add('active');
        }
    });
}

// ============================================================
// UI HELPERS
// ============================================================

function showLoading(show) {
    document.getElementById('loadingState').style.display = show ? 'flex' : 'none';
    document.getElementById('welcomeState').style.display = show ? 'none' : '';
    document.getElementById('resultsTabs').style.display = 'none';

    const btn = document.getElementById('analyzeBtn');
    if (show) {
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Analyzing...';
        btn.disabled = true;
    } else {
        btn.innerHTML = '<i class="fas fa-play"></i> Run & Analyze';
        btn.disabled = false;
    }
}

function showError(msg) {
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('welcomeState').style.display = 'none';
    document.getElementById('resultsTabs').style.display = 'block';
    showResultTab('output');

    document.getElementById('outputSuccess').style.display = 'none';
    document.getElementById('outputError').style.display = 'block';
    document.getElementById('errorText').textContent = msg;
}

/**
 * Logout - clear token and redirect to login.
 */
function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
