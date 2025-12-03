package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.mapper.DeviceMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.repository.DeviceRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Service
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper deviceMapper;

    private OkHttpClient client = new OkHttpClient();

    @Value("${app.thingsboard.url}")
    private String thingsboardUrl;

    private String provisionKey;
    private String provisionSecret;

    public DeviceService(DeviceRepository repository, DeviceMapper deviceMapper){
        this.repository = repository;
        this.deviceMapper = deviceMapper;
    }

    @Transactional
    public DeviceDto createDevice(CreateDeviceRequest request){
        Device device = new Device();
        device.setId(request.macAddress());
        device.setName(Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .orElse("Device" + request.macAddress()));
        device.setOnline(false);

        try {
            device.setAccessToken(createDeviceOnThingsboard(device.getId()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return deviceMapper.toDto(repository.save(device));
    }

    private String createDeviceOnThingsboard(String name) throws IOException {
        String endpoint = String.format("%s/api/v1/provision", thingsboardUrl);

        MediaType jsonType = MediaType.get("application/json; charset=utf-8");

        String json = String.format("""
        {
          "deviceName": "%s",
          "provisionDeviceKey": "%s",
          "provisionDeviceSecret": "%s"
        }
        """, name, provisionKey, provisionSecret);

        RequestBody body = RequestBody.create(json, jsonType);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        String respBody;
        try (Response response = client.newCall(request).execute()) {
            respBody = Objects.requireNonNull(response.body()).string();
        }

        JsonObject jsonObj = JsonParser.parseString(respBody).getAsJsonObject();

        return jsonObj.get("credentialsValue").getAsString();
    }

    public DeviceDto getDevice(String deviceId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }
        return deviceMapper.toDto(device);
    }

    public DeviceDto updateDevice(String deviceId, UpdateDeviceRequest request) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device != null){
            device.setName(request.name());
            device.setOnline(request.isOnline());
        } else{
            throw new RuntimeException("Device not found");
        }

        return deviceMapper.toDto(repository.save(device));
    }

    public String deleteDevice(String deviceId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device != null){
            repository.deleteById(deviceId);
        } else{
            throw new RuntimeException("Device not found");
        }

        // Delete device on thingsboard
        //TODO

        return deviceId;
    }
}
