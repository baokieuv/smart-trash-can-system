package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {

}
