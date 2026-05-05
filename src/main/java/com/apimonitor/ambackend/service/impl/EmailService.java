package com.apimonitor.ambackend.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public EmailService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendAlert(String toEmail, String url) {
        log.info("Sending alert email to: {} for URL: {}", toEmail, url);
        try {
            Map<String, Object> body = Map.of(
                    "sender", Map.of("email", fromEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", "API Alert: Endpoint is DOWN",
                    "textContent", "Your monitored API is currently unreachable.\n\nEndpoint: " + url
                            + "\n\nPlease check your service immediately.");

            webClient.post()
                    .uri(BREVO_API_URL)
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Alert email sent successfully to: {}", toEmail);
        } catch (WebClientResponseException e) {
            log.error("Failed to send alert email to: {} — HTTP {}: {}", toEmail, e.getStatusCode(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to send alert email to: {}", toEmail, e);
        }
    }
}
