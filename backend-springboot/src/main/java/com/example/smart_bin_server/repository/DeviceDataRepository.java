package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceDataRepository extends JpaRepository<DeviceData, String> {
    Optional<DeviceData> findFirstByDeviceIdOrderByTimestampDesc(String deviceId);
}
