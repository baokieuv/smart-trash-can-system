package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.UpdateNotiStatus;
import com.example.smart_bin_server.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private NotificationService service;

    public NotificationController(NotificationService service){
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Object> getNotifications(@AuthenticationPrincipal Jwt jwt){
        String userId = jwt.getSubject();
        return ResponseEntity.ok().body(service.getNotificationsByUserId(userId));
    }

    @PutMapping("/{notiId}")
    public ResponseEntity<Object> changeNotificationStatus(
            @PathVariable Long notiId,
            @Valid @RequestBody UpdateNotiStatus request){
        return ResponseEntity.ok().body(service.changeStatusNotification(notiId, request));
    }
//    @GetMapping
//    public ResponseEntity<Object> getLogs(){
//        return ResponseEntity.ok().body(service.getNotifications());
//    }
}
