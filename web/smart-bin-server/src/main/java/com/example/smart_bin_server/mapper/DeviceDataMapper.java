package com.example.smart_bin_server.mapper;

import com.example.smart_bin_server.dto.DeviceDataDTO;
import com.example.smart_bin_server.model.DeviceData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceDataMapper {
    DeviceDataDTO toDto(DeviceData data);
    DeviceData toEntity(DeviceDataDTO dto);
}
