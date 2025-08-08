package com.flashcards.security;

import com.flashcards.model.User;
import com.flashcards.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find user by email first, then by ID
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findById(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email/id: " + username)));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Use email as the username for Spring Security
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
} 