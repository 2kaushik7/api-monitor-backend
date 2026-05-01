package com.apimonitor.ambackend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apimonitor.ambackend.model.Endpoint;

@Repository
public interface EndpointRepository extends JpaRepository<Endpoint, UUID> {
}
