package org.example.subsidyplatformtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (user.getAge() <= 0 || user.getAge() > 120) {
            throw new UserRegistrationException("Registration Failed: Invalid age values.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserRegistrationException("Registration Failed: Email is already registered!");
        }
        if (userRepository.findByGovernmentId(user.getGovernmentId()).isPresent()) {
            throw new UserRegistrationException("Registration Failed: Government ID already exists!");
        }

        // Encrypt the password securely before saving to DB
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
