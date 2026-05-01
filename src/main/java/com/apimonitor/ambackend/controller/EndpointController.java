package com.apimonitor.ambackend.controller;

import org.springframework.web.bind.annotation.RestController;

import com.apimonitor.ambackend.model.Endpoint;
import com.apimonitor.ambackend.service.EndpointService;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/apis")
@CrossOrigin(origins = "*")
public class EndpointController {

    private static final Logger log = LoggerFactory.getLogger(EndpointController.class);

    @Autowired
    EndpointService endpointService;

    EndpointController(EndpointService endpointService){
        this.endpointService = endpointService;
    }

    @GetMapping("/get-apis")
    public ResponseEntity<List<String>> getAllApis() {
        log.info("GET /get-apis");
        return ResponseEntity.ok(endpointService.getAllApis());
    }

    @GetMapping("/get-endpoints")
    public ResponseEntity<List<Endpoint>> getAllEndpoints() {
        log.info("GET /get-endpoints");
        return ResponseEntity.ok(endpointService.getAllEndpoints());
    }

    @PostMapping("/add-api")
    public ResponseEntity<Endpoint> addEndpoint(@RequestBody Endpoint endpoint) {
        log.info("POST /add-api - url: {}", endpoint.getUrl());
        return ResponseEntity.ok(endpointService.addEndpoint(endpoint));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Endpoint> updateEndpoint(@PathVariable UUID id, @RequestBody Endpoint endpoint) {
        log.info("PUT /{} - url: {}", id, endpoint.getUrl());
        return ResponseEntity.ok(endpointService.updateEndpoint(id, endpoint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable UUID id) {
        log.info("DELETE /{}", id);
        endpointService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }


}
