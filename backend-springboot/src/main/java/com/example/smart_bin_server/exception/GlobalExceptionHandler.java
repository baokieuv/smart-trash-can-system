package com.example.smart_bin_server.exception;

import com.example.smart_bin_server.config.Constants;
import com.example.smart_bin_server.model.Notification;
import com.example.smart_bin_server.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final NotificationService notificationService;

    public GlobalExceptionHandler(NotificationService notificationService){
        this.notificationService = notificationService;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e){
        log.error("An unexpected error occurred: {}", e.getMessage());

        Notification notification = new Notification();
        notification.setDeviceId("system");
        notification.setMessage(e.getMessage());
        notification.setType(String.valueOf(Constants.LogType.ERROR));
        notification.setTimestamp(System.currentTimeMillis());
        notificationService.addNotification(notification);

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
