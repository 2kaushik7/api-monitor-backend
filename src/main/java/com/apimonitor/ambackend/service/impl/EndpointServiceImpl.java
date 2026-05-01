package com.apimonitor.ambackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.apimonitor.ambackend.model.Alert;
import com.apimonitor.ambackend.model.Endpoint;
import com.apimonitor.ambackend.repository.AlertRepository;
import com.apimonitor.ambackend.repository.EndpointRepository;
import com.apimonitor.ambackend.repository.UserRepository;
import com.apimonitor.ambackend.service.EndpointService;

@Service
public class EndpointServiceImpl implements EndpointService {

    private static final Logger log = LoggerFactory.getLogger(EndpointServiceImpl.class);

    private final EndpointRepository endpointRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final WebClient webClient = WebClient.builder().build();

    EndpointServiceImpl(EndpointRepository endpointRepository, AlertRepository alertRepository,
            UserRepository userRepository, EmailService emailService) {
        this.endpointRepository = endpointRepository;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public List<String> getAllApis() {
        log.info("Fetching all API URLs");
        List<Endpoint> endpoints = endpointRepository.findAll();
        log.debug("Found {} endpoints", endpoints.size());
        return endpoints.stream()
                .map(Endpoint::getUrl)
                .toList();
    }

    @Override
    public Endpoint addEndpoint(Endpoint endpoint) {
        log.info("Adding endpoint: {}", endpoint.getUrl());
        if (endpoint.getUserEmail() != null && !endpoint.getUserEmail().isBlank()) {
            userRepository.findByEmail(endpoint.getUserEmail())
                    .ifPresent(endpoint::setUser);
        }
        Endpoint saved = endpointRepository.save(endpoint);
        log.debug("Saved endpoint with id: {}", saved.getId());
        return saved;
    }

    @Override
    public List<Endpoint> getAllEndpoints() {
        log.info("Fetching all endpoints");
        List<Endpoint> endpoints = endpointRepository.findAll();
        log.debug("Found {} endpoints", endpoints.size());
        return endpoints;
    }

    @Override
    @Transactional
    public void deleteEndpoint(UUID id) {
        log.info("Deleting endpoint with id: {}", id);
        endpointRepository.findById(id).ifPresent(endpoint -> {
            alertRepository.deleteByEndpoint(endpoint);
            endpointRepository.delete(endpoint);
        });
        log.debug("Deleted endpoint with id: {}", id);
    }

    @Override
    public Endpoint updateEndpoint(UUID id, Endpoint endpoint) {
        log.info("Updating endpoint with id: {}", id);
        Endpoint existing = endpointRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Endpoint not found with id: {}", id);
                    return new RuntimeException("Endpoint not found: " + id);
                });
        existing.setUrl(endpoint.getUrl());
        existing.setStatus(endpoint.getStatus());
        Endpoint updated = endpointRepository.save(existing);
        log.debug("Updated endpoint: {}", updated.getUrl());
        return updated;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkEndpoints() {
        log.info("Running scheduled endpoint check");
        List<Endpoint> endpoints = endpointRepository.findAll();
        log.debug("Checking {} endpoints", endpoints.size());

        for (Endpoint endpoint : endpoints) {
            long start = System.currentTimeMillis();
            try {
                webClient.get()
                        .uri(endpoint.getUrl())
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                long responseTime = System.currentTimeMillis() - start;
                log.info("Endpoint UP: {} ({}ms)", endpoint.getUrl(), responseTime);
                saveCheck(endpoint, "UP", responseTime);

            } catch (Exception e) {
                log.warn("Endpoint DOWN: {} - {}", endpoint.getUrl(), e.getMessage());
                saveCheck(endpoint, "DOWN", 0);
                saveAlert(endpoint);
                if (endpoint.getAlertEmail() != null && !endpoint.getAlertEmail().isBlank()) {
                    emailService.sendAlert(endpoint.getAlertEmail(), endpoint.getUrl());
                } else if (endpoint.getUser() != null) {
                    emailService.sendAlert(endpoint.getUser().getEmail(), endpoint.getUrl());
                }
            }
        }
    }

    private void saveCheck(Endpoint endpoint, String status, long responseTime) {
        log.debug("Saving check for endpoint: {} with status: {} and responseTime: {}ms", endpoint.getUrl(), status,
                responseTime);
        endpoint.setStatus(status);
        endpoint.setResponse(responseTime);
        endpointRepository.save(endpoint);
    }

    private void saveAlert(Endpoint endpoint) {
        log.warn("Saving alert for endpoint: {}", endpoint.getUrl());
        Alert alert = new Alert();
        alert.setEndpoint(endpoint);
        alert.setMessage("Endpoint DOWN: " + endpoint.getUrl());
        alert.setAlertedAt(LocalDateTime.now());
        alertRepository.save(alert);
        log.debug("Alert saved for endpoint id: {}", endpoint.getId());
    }
}
