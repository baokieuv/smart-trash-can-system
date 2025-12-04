package com.example.smart_bin_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Device {
    @Id
    private String id;

    private String name;

    private String isOnline;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String isOnline() {
        return isOnline;
    }

    public void setOnline(String online) {
        isOnline = online;
    }
}
