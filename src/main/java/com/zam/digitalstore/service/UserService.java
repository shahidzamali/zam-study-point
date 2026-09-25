package com.zam.digitalstore.service;

import com.zam.digitalstore.model.User;
import com.zam.digitalstore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register User
    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Password ko BCrypt hash me convert karo
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Login User
    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Plain password ko BCrypt hash ke saath verify karo
        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}