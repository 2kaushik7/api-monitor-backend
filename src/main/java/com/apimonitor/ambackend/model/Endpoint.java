package com.apimonitor.ambackend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
public class Endpoint {

    @Id
    @GeneratedValue
    private UUID id;

    private String url;

    private String status;

    @Column(columnDefinition = "bigint default 0")
    private Long response = 0L;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String alertEmail;

    @Transient
    private String userEmail;
} 
