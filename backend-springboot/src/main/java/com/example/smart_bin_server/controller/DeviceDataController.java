package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.service.DeviceDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceDataController {

    private final DeviceDataService deviceDataService;

    public DeviceDataController(DeviceDataService deviceDataService){
        this.deviceDataService = deviceDataService;
    }

    @GetMapping("/{deviceId}/nonce")
    public ResponseEntity<Object> getNonce(@PathVariable String deviceId){
//        String nonce = UUID.randomUUID().toString();
        return ResponseEntity.ok().body(deviceDataService.getNonce(deviceId));
    }

    @PostMapping("/{deviceId}/data")
    public ResponseEntity<Object> sendData(
            @PathVariable String deviceId,
            @RequestHeader("x-signature") String signature,
            @RequestBody String request)
    {
        return ResponseEntity.ok().body(deviceDataService.sendData(deviceId, request, signature));
    }

    @GetMapping("/{deviceId}/data")
    public ResponseEntity<Object> getData(@PathVariable String deviceId){
        return ResponseEntity.ok().body(deviceDataService.getData(deviceId));
    }
}
