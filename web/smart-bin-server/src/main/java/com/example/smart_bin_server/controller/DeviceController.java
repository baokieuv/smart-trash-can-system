package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService){
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<Object> createDevice(@RequestBody CreateDeviceRequest request){
        return ResponseEntity.ok().body(deviceService.createDevice(request));
    }

    @GetMapping
    public ResponseEntity<Object> getDevices(){
        return ResponseEntity.ok().body(deviceService.getDevices());
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Object> getDeviceById(@PathVariable String deviceId){
        return ResponseEntity.ok().body(deviceService.getDeviceById(deviceId));
    }

    @PutMapping("/{deviceId}")
    public ResponseEntity<Object> updateDevice(@PathVariable String deviceId, @Valid @RequestBody UpdateDeviceRequest request){
        return ResponseEntity.ok().body(deviceService.updateDevice(deviceId, request));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Object> deleteDevice(@PathVariable String deviceId){
        return ResponseEntity.ok().body(deviceService.deleteDevice(deviceId));
    }
}
