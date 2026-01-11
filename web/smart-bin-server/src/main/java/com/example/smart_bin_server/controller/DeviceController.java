package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService){
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<Object> createDevice(
            @RequestBody CreateDeviceRequest request,
            @AuthenticationPrincipal Jwt jwt){

        String userId = jwt.getSubject();
        return ResponseEntity.ok().body(deviceService.createDevice(request, userId));
    }

    @GetMapping
    public ResponseEntity<Object> getDevices(
            @AuthenticationPrincipal Jwt jwt){

        String userId = jwt.getSubject();
        return ResponseEntity.ok().body(deviceService.getDevices(userId));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Object> getDeviceById(
            @PathVariable String deviceId,
            @AuthenticationPrincipal Jwt jwt){

        String userId = jwt.getSubject();
        return ResponseEntity.ok().body(deviceService.getDeviceById(deviceId, userId));
    }

    @PutMapping("/{deviceId}")
    public ResponseEntity<Object> updateDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody UpdateDeviceRequest request,
            @AuthenticationPrincipal Jwt jwt){

        String userId = jwt.getSubject();
        return ResponseEntity.ok().body(deviceService.updateDevice(deviceId, request, userId));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Object> deleteDevice(
            @PathVariable String deviceId,
            @AuthenticationPrincipal Jwt jwt){

        String userId = jwt.getSubject();
        return ResponseEntity.ok().body(deviceService.deleteDevice(deviceId, userId));
    }
}
