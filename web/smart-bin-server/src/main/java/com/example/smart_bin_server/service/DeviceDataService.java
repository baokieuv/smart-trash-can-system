package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.SendDataRequest;
import com.example.smart_bin_server.dto.SendDataResponse;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.repository.DeviceRepository;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DeviceDataService {

    @Value("${app.thingsboard.url}")
    private String thingsboardServer;

    private final DeviceRepository repository;

    private OkHttpClient client = new OkHttpClient();

    public DeviceDataService(DeviceRepository repository){
        this.repository = repository;
    }

    public SendDataResponse sendData(String deviceId, SendDataRequest request) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }

        String endpoint = String.format("%s/api/v1/%s/telemetry", thingsboardServer, device.getAccessToken());

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(request.toString(), JSON);

        Request req = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try {
            Response resp = client.newCall(req).execute();
            return new SendDataResponse(deviceId, resp.code(), resp.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
