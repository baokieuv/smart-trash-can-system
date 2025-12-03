package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.ClassificationResponse;
import com.example.smart_bin_server.dto.SendDataResponse;
import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
public class ClassificationService {

    @Value("${app.ai-server.url}")
    private String AIUrl;

    private final OkHttpClient client = new OkHttpClient();

    public ClassificationResponse classify(MultipartFile image) {
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

                return new Gson().fromJson(jsonResp, ClassificationResponse.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
