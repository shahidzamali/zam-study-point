package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.User;
import com.zam.digitalstore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Signup
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        try {

            User savedUser = userService.registerUser(user);

            return ResponseEntity.ok(savedUser);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User user) {

        try {

            User loggedInUser = userService.loginUser(
                    user.getEmail(),
                    user.getPassword()
            );

            return ResponseEntity.ok(loggedInUser);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}