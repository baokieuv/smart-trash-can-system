package com.example.smart_bin_server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "logs")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String deviceId;

    private String userId;

    private String deviceName;

    private String message;

    private String type;

    private Long timestamp;

    private Boolean isRead = false;
}
