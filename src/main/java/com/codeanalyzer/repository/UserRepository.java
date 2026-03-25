package com.codeanalyzer.repository;

import com.codeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UserRepository - handles all database operations for User entity.
 * Spring Data JPA automatically provides CRUD methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username (used for login)
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if username already exists
    boolean existsByUsername(String username);

    // Check if email already exists
    boolean existsByEmail(String email);
}
