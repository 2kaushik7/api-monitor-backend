package com.apimonitor.ambackend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "checks")
public class Check {

    @Id
    @GeneratedValue
    private UUID id;

    private String status; // UP / DOWN

    private int responseTime;

    private LocalDateTime checkedAt;

    @ManyToOne
    @JoinColumn(name = "endpoint_id")
    private Endpoint endpoint;

}
