package com.example.smart_bin_server.service;

import com.example.smart_bin_server.config.Constants;
import com.example.smart_bin_server.dto.DeviceDataDto;
import com.example.smart_bin_server.dto.SendDataRequest;
import com.example.smart_bin_server.dto.SendDataResponse;
import com.example.smart_bin_server.mapper.DeviceDataMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.DeviceData;
import com.example.smart_bin_server.repository.DeviceDataRepository;
import com.example.smart_bin_server.repository.DeviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DeviceDataService {
    private final DeviceRepository repository;

    private final DeviceDataRepository dataRepository;

    private final DeviceDataMapper dataMapper;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String NONCE_CACHE_PREFIX = "nonce:";
    private static final long NONCE_CACHE_TTL = 60;

    @Value("${app.secret-key}")
    private String secretKey;

    public DeviceDataService(DeviceRepository repository,
                             DeviceDataRepository dataRepository,
                             DeviceDataMapper mapper,
                             RedisTemplate<String, String> redisTemplate){
        this.repository = repository;
        this.dataRepository = dataRepository;
        this.dataMapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    public SendDataResponse sendData(String deviceId, String rawJson, String signature) {

        validateSignature(deviceId, rawJson, signature);

        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }

        device.setStatus(String.valueOf(Constants.DeviceStatus.ONLINE));
        repository.save(device);

        SendDataRequest request = null;
        try {
            request = objectMapper.readValue(rawJson, SendDataRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

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

    public DeviceDataDto getData(String deviceId){
        DeviceData data = dataRepository
                .findFirstByDeviceIdOrderByTimestampDesc(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        if(data == null){
            throw new RuntimeException("Device not found");
        }

        return dataMapper.toDto(data);
    }

    public String getNonce(String deviceId){
        String key = NONCE_CACHE_PREFIX + deviceId;

        String cachedNonce = redisTemplate.opsForValue().get(key);
        if(cachedNonce != null){
            return cachedNonce;
        }

        String nonce = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key, nonce, NONCE_CACHE_TTL, TimeUnit.SECONDS);

        return nonce;
    }

    private boolean validateSignature(String deviceId, String signature, String payload){
        String key = NONCE_CACHE_PREFIX + deviceId;
        String cachedNonce = redisTemplate.opsForValue().get(key);

        if(cachedNonce == null){
            throw new RuntimeException("Nonce not found or expired. Please request new nonce.");
        }

        String dataToSign = cachedNonce + "." + payload;
        String serverSignature = new HmacUtils("HmacSHA256", secretKey).hmacHex(dataToSign);

        if(!serverSignature.equalsIgnoreCase(signature)){
            throw new RuntimeException("Invalid Signature! Data tampered.");
        }

        redisTemplate.opsForValue().getAndDelete(key);

        return true;
    }
}
