package com.codeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User entity - represents a registered user in the system.
 * Stored in the 'users' table in MySQL.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username must be unique
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Encrypted password stored here
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // Email address of the user
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Role: USER or ADMIN
    @Column(nullable = false, length = 20)
    private String role = "USER";

    // When the user registered
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Is the account active?
    @Column(name = "is_active")
    private boolean active = true;

    // One user can have many code submissions
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CodeSubmission> submissions;

    public User() {
    }

    public User(Long id, String username, String password, String email, String role, LocalDateTime createdAt, boolean active, List<CodeSubmission> submissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.active = active;
        this.submissions = submissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<CodeSubmission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<CodeSubmission> submissions) {
        this.submissions = submissions;
    }
}
