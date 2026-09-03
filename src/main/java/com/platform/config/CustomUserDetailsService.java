package com.platform.config;

import com.platform.user.User;
import com.platform.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Identity verification missing for: " + email);
        }

        User user = userOpt.get();

        // FIX: Strips pre-existing 'ROLE_' tags dynamically to prevent token formatting collisions
        String[] parsedRoles = user.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(parsedRoles)
                .build();
    }
}
