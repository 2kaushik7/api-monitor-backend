package com.apimonitor.ambackend.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "alerts@apimonitor.com");
    }

    @Test
    void sendAlert_sendsEmailSuccessfully() {
        emailService.sendAlert("user@example.com", "https://example.com/api");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAlert_sendsToMultipleDifferentRecipients() {
        emailService.sendAlert("user1@example.com", "https://api1.com");
        emailService.sendAlert("user2@example.com", "https://api2.com");
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAlert_doesNotThrow_whenMailSenderThrowsException() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendAlert("user@example.com", "https://example.com/api");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAlert_logsWarning_onMailException() {
        doThrow(new MailSendException("Bad Request")).when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendAlert("user@example.com", "https://example.com/api");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}

