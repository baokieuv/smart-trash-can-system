package com.example.smart_bin_server.mapper;

import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.model.Device;

public interface DeviceMapper {
    DeviceDto toDto(Device device);
}
