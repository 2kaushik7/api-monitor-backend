package com.apimonitor.ambackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${brevo.from.email}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(String toEmail, String url) {
        log.info("Sending alert email to: {} for URL: {}", toEmail, url);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("API Alert: Endpoint is DOWN");
            message.setText("Your monitored API is currently unreachable.\n\nEndpoint: " + url
                    + "\n\nPlease check your service immediately.");
            mailSender.send(message);
            log.info("Alert email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send alert email to: {}", toEmail, e);
        }
    }
}
