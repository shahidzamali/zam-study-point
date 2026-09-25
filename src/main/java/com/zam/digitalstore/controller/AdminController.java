package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.Admin;
import com.zam.digitalstore.repository.AdminRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminController(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // ADMIN LOGIN
    // POST: /api/admin/login
    // =====================================================

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password) {

        Admin admin = adminRepository
                .findByUsername(username)
                .orElse(null);

        if (admin == null) {
            return "INVALID";
        }

        if (!passwordEncoder.matches(
                password,
                admin.getPassword())) {

            return "INVALID";
        }

        return "SUCCESS";
    }
}