package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.ClassificationResponse;
import com.example.smart_bin_server.dto.SendDataResponse;
import com.example.smart_bin_server.model.ClassificationLogs;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.repository.ClassificationLogsRepository;
import com.example.smart_bin_server.repository.DeviceRepository;
import com.example.smart_bin_server.repository.UserRepository;
import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
public class ClassificationService {

    @Value("${app.ai-server.url}")
    private String AIUrl;

    private final OkHttpClient client = new OkHttpClient();

    private final ClassificationLogsRepository repository;

    private final DeviceRepository deviceRepository;

    private final MinioService minioService;

    public ClassificationService(ClassificationLogsRepository repository, DeviceRepository deviceRepository, MinioService minioService){
        this.repository = repository;
        this.deviceRepository = deviceRepository;
        this.minioService = minioService;
    }

    public ClassificationResponse classify(MultipartFile image, String deviceId) {
        String endpoint = String.format("%s/classify", AIUrl);

        try {
            RequestBody body = RequestBody.create(image.getBytes(), MediaType.parse(Objects.requireNonNull(image.getContentType())));

             MultipartBody requestBody = new MultipartBody.Builder()
                     .setType(MultipartBody.FORM)
                     .addFormDataPart("image", image.getOriginalFilename(), body)
                     .build();

             Request req = new Request.Builder()
                     .url(endpoint)
                     .post(requestBody)
                     .build();

            try (Response resp = client.newCall(req).execute()) {

                String jsonResp = Objects.requireNonNull(resp.body()).string();  // Ví dụ: {"Label": "cup", "Confident": 0.83, "Category": "recyclable"}

                ClassificationResponse response = new Gson().fromJson(jsonResp, ClassificationResponse.class);

                try {
                    String imageUrl = minioService.uploadFile(image);

                    Device device = deviceRepository.findById(deviceId).orElse(new Device());

                    ClassificationLogs logs = new ClassificationLogs();
                    logs.setDeviceId(deviceId);
                    logs.setUserId(device.getUserId());
                    logs.setImageUrl(imageUrl);
                    logs.setLabel(response.Label());
                    logs.setCategory(response.Category());
                    logs.setConfidence(response.Confident());

                    repository.save(logs);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }

                return response;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
