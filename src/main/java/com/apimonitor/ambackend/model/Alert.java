package com.apimonitor.ambackend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "alerts")
@Data
public class Alert {

    @Id
    @GeneratedValue
    private UUID id;

    private String message;

    private LocalDateTime alertedAt;

    @ManyToOne
    @JoinColumn(name = "endpoint_id")
    private Endpoint endpoint;
}
