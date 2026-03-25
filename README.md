# 🤖 AI-Powered Multi-Language Code Analyzer & Learning Assistant

A full-stack Spring Boot application that lets users write, execute, and analyze Java, C, and Python code with AI-powered beginner-friendly explanations.

---

## 📋 TABLE OF CONTENTS

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Setup in STS (Spring Tool Suite)](#setup-in-sts)
5. [Database Setup](#database-setup)
6. [Running the Application](#running-the-application)
7. [Default Login Credentials](#default-login-credentials)
8. [AI API Configuration](#ai-api-configuration)
9. [Project Structure](#project-structure)
10. [API Endpoints](#api-endpoints)
11. [Troubleshooting](#troubleshooting)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 Authentication | Login/Register with JWT tokens, USER & ADMIN roles |
| ☕🐍⚙️ Multi-Language | Execute Java, C, and Python code |
| 📊 Code Analysis | Lines, cyclomatic complexity, methods, variables, constructors |
| 📚 Beginner Explanations | Step-by-step code walkthrough in simple English |
| ⚙️ Workflow Explanation | How the output was produced |
| 🤖 AI Assistant | Ask questions about code (needs API key) |
| 📺 Video Suggestions | YouTube tutorials based on code concepts |
| 📜 History | View all past submissions |
| 👑 Admin Dashboard | User management, analytics, all submissions |
| 🔒 Security | Execution timeout, dangerous command blocking |

---

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2.0 (Java 17, Maven)
- **Frontend**: HTML5, CSS3, JavaScript, CodeMirror
- **Database**: MySQL 8.x
- **Security**: Spring Security + JWT
- **AI**: Claude API (Anthropic) - optional
- **Build**: Maven

---

## 📦 Prerequisites

Before running the project, make sure you have installed:

1. **Java 17** (or higher)
   - Verify: `java -version`
   - Download: https://adoptium.net/

2. **Maven 3.8+**
   - Verify: `mvn -version`
   - Usually bundled with STS

3. **MySQL 8.x**
   - Verify: `mysql --version`
   - Download: https://dev.mysql.com/downloads/

4. **Spring Tool Suite (STS) 4**
   - Download: https://spring.io/tools

5. **For running C code**: GCC compiler
   - Linux/Mac: `sudo apt install gcc` or `brew install gcc`
   - Windows: Install MinGW or use WSL

6. **For running Python**: Python 3.x
   - Verify: `python3 --version`
   - Download: https://www.python.org/downloads/

---

## 🗄️ Database Setup

### Option A: Let Spring Boot create tables automatically (Recommended)

The `application.properties` already has:
```
spring.jpa.hibernate.ddl-auto=update
```
This automatically creates all tables when you run the app.

Just create the database first:
```sql
CREATE DATABASE code_analyzer_db;
```

### Option B: Run the SQL script manually

1. Open MySQL Workbench or MySQL CLI
2. Run the provided `database-schema.sql` file:

```bash
mysql -u root -p < database-schema.sql
```

---

## 🚀 Setup in STS (Spring Tool Suite)

### Step 1: Import the Project

1. Open STS
2. Go to **File → Import → Maven → Existing Maven Projects**
3. Browse to the project folder (where `pom.xml` is)
4. Click **Finish**
5. Wait for Maven to download all dependencies (~2-5 minutes first time)

### Step 2: Configure Database

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/code_analyzer_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root        # ← Change to your MySQL username
spring.datasource.password=root        # ← Change to your MySQL password
```

### Step 3: Configure AI API Key (Optional)

Get a free API key from https://console.anthropic.com and add it:

```properties
app.ai.api.key=your_actual_api_key_here
```

**Without an API key**: The app still works fully — AI explanations will use built-in templates.

### Step 4: Run the Application

**In STS:**
1. Right-click on `CodeAnalyzerApplication.java`
2. Select **Run As → Spring Boot App**
3. Watch the console for: `AI Code Analyzer is running!`

**Via Maven:**
```bash
mvn spring-boot:run
```

### Step 5: Open in Browser

Go to: **http://localhost:8080**

You'll be redirected to the login page.

---

## 🔑 Default Login Credentials

These accounts are created automatically on first startup:

| Role | Username | Password |
|------|----------|----------|
| ADMIN | `admin` | `admin123` |
| USER | `user` | `user123` |

---

## 🤖 AI API Configuration

### Getting a Claude API Key

1. Visit https://console.anthropic.com
2. Create an account (free tier available)
3. Go to **API Keys** → Create new key
4. Copy the key

### Adding the Key

In `src/main/resources/application.properties`:

```properties
app.ai.api.key=sk-ant-api03-your-key-here
```

Restart the application after changing this.

---

## 📁 Project Structure

```
code-analyzer/
├── pom.xml                          ← Maven dependencies
├── database-schema.sql              ← MySQL schema
├── README.md                        ← This file
└── src/
    └── main/
        ├── java/com/codeanalyzer/
        │   ├── CodeAnalyzerApplication.java    ← Main class
        │   ├── config/
        │   │   ├── SecurityConfig.java         ← Spring Security setup
        │   │   └── DataInitializer.java        ← Creates default users
        │   ├── controller/
        │   │   ├── AuthController.java         ← Login/Register API
        │   │   ├── CodeAnalyzerController.java ← Code submit API
        │   │   └── AdminController.java        ← Admin API
        │   ├── dto/
        │   │   └── Dtos.java                   ← Request/Response objects
        │   ├── entity/
        │   │   ├── User.java                   ← User database entity
        │   │   └── CodeSubmission.java         ← Submission entity
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   └── CodeSubmissionRepository.java
        │   ├── security/
        │   │   ├── JwtUtils.java               ← JWT token utilities
        │   │   └── JwtAuthFilter.java          ← JWT request filter
        │   └── service/
        │       ├── AuthService.java            ← Login/Register logic
        │       ├── CodeExecutionService.java   ← Runs user code
        │       ├── CodeAnalysisService.java    ← Metrics + explanations
        │       ├── AiService.java              ← Claude AI integration
        │       ├── CodeAnalyzerService.java    ← Main orchestrator
        │       ├── AdminService.java           ← Admin functions
        │       └── CustomUserDetailsService.java
        └── resources/
            ├── application.properties          ← Configuration
            └── static/
                ├── index.html                  ← Redirect page
                ├── login.html                  ← Login/Register page
                ├── analyzer.html               ← Main code editor
                ├── history.html                ← Submission history
                ├── admin.html                  ← Admin dashboard
                ├── css/
                │   └── style.css               ← All styles
                └── js/
                    ├── auth.js                 ← Login/Register logic
                    ├── analyzer.js             ← Editor & analysis
                    ├── history.js              ← History page
                    └── admin.js                ← Admin dashboard
```

---

## 🔌 API Endpoints

### Authentication (Public)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Create new account |
| POST | `/api/auth/login` | Login, get JWT token |

### Code Analysis (Authenticated)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/code/analyze` | Submit code for analysis |
| GET | `/api/code/history` | Get user's history |
| GET | `/api/code/submission/{id}` | Get submission details |
| POST | `/api/code/ai-chat` | Ask AI about code |

### Admin (ADMIN role only)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/admin/analytics` | Dashboard statistics |
| GET | `/api/admin/users` | All users |
| GET | `/api/admin/submissions` | All submissions |
| PUT | `/api/admin/users/{id}/toggle` | Enable/disable user |

---

## 🔧 Troubleshooting

### Problem: "Access denied for user 'root'@'localhost'"
**Solution**: Update database credentials in `application.properties`

### Problem: Java code won't compile
**Solution**: Make sure Java JDK (not just JRE) is installed and `javac` is in PATH

### Problem: C code won't compile
**Solution**: Install GCC compiler:
- Ubuntu/Debian: `sudo apt-get install gcc`
- Mac: `xcode-select --install`
- Windows: Install MinGW from https://www.mingw-w64.org/

### Problem: Python code won't run
**Solution**: Install Python 3: https://www.python.org/downloads/

### Problem: "White label error page" on startup
**Solution**: Check MySQL is running and credentials in application.properties are correct

### Problem: AI explanations show "Configure API key"
**Solution**: Add your Anthropic API key to application.properties and restart

### Problem: Port 8080 already in use
**Solution**: Change port in application.properties:
```properties
server.port=8090
```

---

## 🎯 How to Use

1. **Login** at http://localhost:8080 (use `user` / `user123`)
2. **Select a language** (Java, C, or Python)
3. **Write or paste code** in the editor
4. **Click "Run & Analyze"** (or press Ctrl+Enter)
5. **View results** in the tabs:
   - **Output**: Execution result
   - **Metrics**: Code statistics
   - **Explanation**: Beginner-friendly walkthrough
   - **Workflow**: How the output was produced
   - **AI Chat**: Ask questions about the code
   - **Videos**: Tutorial recommendations
6. **History** tab shows all past submissions
7. **Admin** (if ADMIN role): View all users and submissions

---

## 📝 Notes

- Code execution has a **10-second timeout** to prevent infinite loops
- Dangerous commands (like system calls) are **blocked** for security
- All code and results are **saved to MySQL** automatically
- JWT tokens expire after **24 hours** (configurable in application.properties)

---

## 📄 License

This project is built for educational purposes.
