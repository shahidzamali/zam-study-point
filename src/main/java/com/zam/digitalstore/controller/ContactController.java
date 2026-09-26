package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.ContactRequest;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin("*")
public class ContactController {

    private final JavaMailSender mailSender;

    public ContactController(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    @PostMapping
    public String sendContactMessage(
            @RequestBody ContactRequest request) {

        // Email service is not configured on Render Free
        if (mailSender == null) {
            return "Contact message received successfully";
        }

        try {

            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setTo("mdshahidzamali@gmail.com");

            mail.setSubject(
                    "Zam Digital Store - " + request.getSubject());

            mail.setText(
                    "New Contact Message\n\n" +
                            "Name: " + request.getName() + "\n" +
                            "Email / Phone: " + request.getEmail() + "\n\n" +
                            "Subject: " + request.getSubject() + "\n\n" +
                            "Message:\n" +
                            request.getMessage());

            mailSender.send(mail);

            return "Message sent successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Failed to send message";
        }
    }
}