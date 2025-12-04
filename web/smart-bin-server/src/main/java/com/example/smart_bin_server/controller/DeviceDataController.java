package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.SendDataRequest;
import com.example.smart_bin_server.service.DeviceDataService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceDataController {

    private final DeviceDataService deviceDataService;

    public DeviceDataController(DeviceDataService deviceDataService){
        this.deviceDataService = deviceDataService;
    }

    @PostMapping("/{deviceId}/data")
    public ResponseEntity<Object> sendData(
            @PathVariable String deviceId,
            @Valid @RequestBody SendDataRequest request)
    {
        return ResponseEntity.ok().body(deviceDataService.sendData(deviceId, request));
    }

    @GetMapping("/{deviceId}/data")
    public ResponseEntity<Object> getData(@PathVariable String deviceId){
        return ResponseEntity.ok().body(deviceDataService.getData(deviceId));
    }
}
