package com.example.smart_bin_server.mapper;

import com.example.smart_bin_server.dto.DeviceDataDTO;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.model.Device;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    DeviceDto toDto(Device device);
    Device toEntity(DeviceDataDTO dto);
}
