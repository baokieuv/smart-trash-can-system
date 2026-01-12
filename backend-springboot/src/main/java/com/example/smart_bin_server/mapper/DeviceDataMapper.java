package com.example.smart_bin_server.mapper;

import com.example.smart_bin_server.dto.DeviceDataDto;
import com.example.smart_bin_server.model.DeviceData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceDataMapper {
    DeviceDataDto toDto(DeviceData data);
    DeviceData toEntity(DeviceDataDto dto);
}
