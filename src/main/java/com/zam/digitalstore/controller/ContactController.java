
package com.zam.digitalstore.controller;

import com.zam.digitalstore.model.ContactRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin("*")
public class ContactController {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${RESEND_TO_EMAIL}")
    private String recipientEmail;

    private final RestClient restClient = RestClient.create();

    @PostMapping
    public String sendContactMessage(
            @RequestBody ContactRequest request) {

        try {
            Map<String, Object> email = Map.of(
                    "from", "Zam Study Point <onboarding@resend.dev>",
                    "to", List.of(recipientEmail),
                    "reply_to", request.getEmail(),
                    "subject", "Zam Study Point - "
                            + request.getSubject(),
                    "text", "Name: " + request.getName()
                            + "\nEmail / Phone: "
                            + request.getEmail()
                            + "\nSubject: "
                            + request.getSubject()
                            + "\nMessage: "
                            + request.getMessage());

            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(email)
                    .retrieve()
                    .toBodilessEntity();

            return "Message sent successfully";

        } catch (Exception e) {
            System.err.println(
                    "Resend email failed: " + e.getMessage());
            return "Failed to send message";
        }
    }
}
