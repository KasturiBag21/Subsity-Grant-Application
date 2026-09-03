package com.platform.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // REQUIRED IMPORT
import java.util.Collections;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * @Transactional forces HikariCP to commit transaction changes
     * directly to MySQL, preventing silent rollbacks.
     */
    @Transactional
    public User registerUser(User user) {
        Beneficiary beneficiary = user.getBeneficiary();
        if (beneficiary == null) {
            throw new UserRegistrationException("Registration Failed: Profile demographic data missing.");
        }

        if (beneficiary.getAge() <= 0 || beneficiary.getAge() > 120) {
            throw new UserRegistrationException("Registration Failed: Invalid age values.");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserRegistrationException("Registration Failed: Email is already registered!");
        }

        if (userRepository.existsByGovernmentId(beneficiary.getGovernmentId())) {
            throw new UserRegistrationException("Registration Failed: Government ID already exists!");
        }

        // Encrypt the password securely before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singleton("ROLE_BENEFICIARY"));
        }

        // Saves BOTH tables safely due to CascadeType.ALL rules
        return userRepository.save(user);
    }
}
