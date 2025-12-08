package com.example.smart_bin_server.service;

import com.example.smart_bin_server.config.Constants;
import com.example.smart_bin_server.dto.DeviceDataDTO;
import com.example.smart_bin_server.dto.SendDataRequest;
import com.example.smart_bin_server.dto.SendDataResponse;
import com.example.smart_bin_server.mapper.DeviceDataMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.DeviceData;
import com.example.smart_bin_server.repository.DeviceDataRepository;
import com.example.smart_bin_server.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
public class DeviceDataService {
    private final DeviceRepository repository;

    private final DeviceDataRepository dataRepository;

    private final DeviceDataMapper dataMapper;

    public DeviceDataService(DeviceRepository repository, DeviceDataRepository dataRepository, DeviceDataMapper mapper){
        this.repository = repository;
        this.dataRepository = dataRepository;
        this.dataMapper = mapper;
    }

    public SendDataResponse sendData(String deviceId, SendDataRequest request) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }

        device.setStatus(String.valueOf(Constants.DeviceStatus.ONLINE));
        repository.save(device);

        DeviceData data = new DeviceData();
        data.setDeviceId(deviceId);
        data.setRecycledWasteCount(request.recycledWasteCount());
        data.setNonRecycledWasteCount(request.nonRecycledWasteCount());
        data.setCompostableWasteCount(request.compostableWasteCount());
        data.setFillLevel(request.fillLevel());
        data.setFull(request.isFull());
        data.setTimestamp(System.currentTimeMillis());

        dataRepository.save(data);

        return new SendDataResponse(deviceId, 200, "Successfully", data.getTimestamp());
    }

    public DeviceDataDTO getData(String deviceId){
        DeviceData data = dataRepository.findById(deviceId).orElse(null);
        if(data == null){
            throw new RuntimeException("Device not found");
        }

        return dataMapper.toDto(data);
    }
}
