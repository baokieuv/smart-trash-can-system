package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceDataRepository extends JpaRepository<DeviceData, String> {
}
