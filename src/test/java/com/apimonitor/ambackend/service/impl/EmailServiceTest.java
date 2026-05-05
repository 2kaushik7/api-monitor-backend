package com.apimonitor.ambackend.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private EmailService emailService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"messageId\":\"test\"}"));

        ReflectionTestUtils.setField(emailService, "fromEmail", "alerts@apimonitor.com");
        ReflectionTestUtils.setField(emailService, "apiKey", "test-api-key");
    }

    @Test
    void sendAlert_sendsEmailSuccessfully() {
        emailService.sendAlert("user@example.com", "https://example.com/api");
        verify(webClient, times(1)).post();
    }

    @Test
    void sendAlert_sendsToMultipleDifferentRecipients() {
        emailService.sendAlert("user1@example.com", "https://api1.com");
        emailService.sendAlert("user2@example.com", "https://api2.com");
        verify(webClient, times(2)).post();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendAlert_doesNotThrow_whenApiReturnsError() {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(WebClientResponseException.create(400, "Bad Request", null, null, null)));
        emailService.sendAlert("user@example.com", "https://example.com/api");
        verify(webClient, times(1)).post();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendAlert_doesNotThrow_whenConnectionFails() {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));
        emailService.sendAlert("user@example.com", "https://example.com/api");
        verify(webClient, times(1)).post();
    }
}

