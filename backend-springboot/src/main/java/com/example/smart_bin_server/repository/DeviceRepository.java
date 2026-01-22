package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByStatus(String status);
    List<Device> findByUserId(String userId);
}
