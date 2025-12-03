package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService){
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@RequestBody CreateDeviceRequest request){
        DeviceDto response = deviceService.createDevice(request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<DeviceDto> getDevice(@RequestParam String deviceId){
        return ResponseEntity.ok().body(deviceService.getDevice(deviceId));
    }

    @PutMapping
    public ResponseEntity<DeviceDto> updateDevice(@RequestParam String deviceId, @RequestBody UpdateDeviceRequest request){
        return ResponseEntity.ok().body(deviceService.updateDevice(deviceId, request));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteDevice(@RequestParam String deviceId){
        return ResponseEntity.ok().body(deviceService.deleteDevice(deviceId));
    }
}
