package com.zam.digitalstore.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "shahid@123";

        String hash = encoder.encode(password);

        System.out.println("BCrypt Hash:");
        System.out.println(hash);
    }
}