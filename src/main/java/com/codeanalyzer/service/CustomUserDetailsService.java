package com.codeanalyzer.service;

import com.codeanalyzer.entity.User;
import com.codeanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * CustomUserDetailsService - Loads user details from MySQL database.
 * Spring Security calls this to get user info during authentication.
 *
 * It implements UserDetailsService which Spring Security understands.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username for Spring Security.
     * Called automatically during login.
     *
     * @param username - the username from login form
     * @return UserDetails object with user info and roles
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Find user in MySQL database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Convert role to Spring Security GrantedAuthority format
        // "ADMIN" becomes "ROLE_ADMIN", "USER" becomes "ROLE_USER"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        // Return Spring Security UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true, true, true,
                Collections.singletonList(authority)
        );
    }
}
