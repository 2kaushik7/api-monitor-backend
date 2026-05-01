package com.apimonitor.ambackend.service;

import java.util.List;
import java.util.UUID;

import com.apimonitor.ambackend.model.Endpoint;

public interface EndpointService{
    public List<String> getAllApis();
    public Endpoint addEndpoint(Endpoint endpoint);
    public List<Endpoint> getAllEndpoints();
    public void deleteEndpoint(UUID id);
    public Endpoint updateEndpoint(UUID id, Endpoint endpoint);
}
