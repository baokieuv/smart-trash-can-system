package com.example.smart_bin_server.exception;

import com.example.smart_bin_server.config.Constants;
import com.example.smart_bin_server.model.Log;
import com.example.smart_bin_server.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final LogService logService;

    public GlobalExceptionHandler(LogService logService){
        this.logService = logService;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e){
        log.error("An unexpected error occurred: {}", e.getMessage());

        Log log = new Log();
        log.setDeviceId("");
        log.setMessage(e.getMessage());
        log.setType(String.valueOf(Constants.LogType.ERROR));
        log.setTimestamp(System.currentTimeMillis());
        logService.addLog(log);

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
