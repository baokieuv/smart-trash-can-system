package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private NotificationService service;

    public NotificationController(NotificationService service){
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Object> getLogs(){
        return ResponseEntity.ok().body(service.getNotifications());
    }
}
