package com.codeanalyzer.config;

import com.codeanalyzer.entity.User;
import com.codeanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - Runs once on application startup.
 *
 * Creates default accounts if they don't exist:
 * - Admin: username=admin, password=admin123
 * - Test User: username=user, password=user123
 *
 * This makes it easy to test the application immediately!
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // Create default ADMIN account if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@codeanalyzer.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setActive(true);
            userRepository.save(admin);

            System.out.println("✅ Default ADMIN account created:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
        }

        // Create custom ADMIN account for Raghu
        if (!userRepository.existsByUsername("Raghu")) {
            User admin2 = new User();
            admin2.setUsername("Raghu");
            admin2.setEmail("raghu@codeanalyzer.com");
            admin2.setPassword(passwordEncoder.encode("123456"));
            admin2.setRole("ADMIN");
            admin2.setActive(true);
            userRepository.save(admin2);

            System.out.println("✅ Custom ADMIN account created:");
            System.out.println("   Username: Raghu");
            System.out.println("   Password: 123456");
        }

        // Create default USER account for testing
        if (!userRepository.existsByUsername("user")) {
            User testUser = new User();
            testUser.setUsername("user");
            testUser.setEmail("user@codeanalyzer.com");
            testUser.setPassword(passwordEncoder.encode("user123"));
            testUser.setRole("USER");
            testUser.setActive(true);
            userRepository.save(testUser);

            System.out.println("✅ Default USER account created:");
            System.out.println("   Username: user");
            System.out.println("   Password: user123");
        }

        System.out.println("==============================================");
        System.out.println("  Database initialized successfully!");
        System.out.println("  Open browser: http://localhost:8080");
        System.out.println("==============================================");
    }
}
