package com.example.smart_bin_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "device")
public class Device {
    @Id
    private String id;

    private String name;

    private boolean isOnline;

    private String accessToken;
}
