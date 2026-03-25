package com.codeanalyzer.config;

import com.codeanalyzer.security.JwtAuthFilter;
import com.codeanalyzer.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Configures all security rules for the application.
 *
 * Key decisions:
 * - Public routes: login, register, static files → no auth needed
 * - /api/admin/** → only ADMIN role can access
 * - Everything else → must be logged in
 * - Session is STATELESS (we use JWT, not session cookies)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Password encoder using BCrypt - safely hashes passwords.
     * BCrypt is recommended because it's slow by design (hard to crack).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider - connects UserDetailsService and PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager - used to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Main security filter chain - defines which URLs need authentication.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF since we use JWT (stateless API)
            .csrf(csrf -> csrf.disable())

            // Define URL access rules
            .authorizeHttpRequests(auth -> auth
                // Public pages - anyone can access
                .requestMatchers("/", "/index.html", "/login.html", "/register.html").permitAll()
                // Public API endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Static resources (CSS, JS, images)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/*.ico").permitAll()
                // Admin endpoints - only ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else needs authentication
                .anyRequest().authenticated()
            )

            // Use stateless session (no session cookies, use JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Add JWT filter before the default username/password filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
