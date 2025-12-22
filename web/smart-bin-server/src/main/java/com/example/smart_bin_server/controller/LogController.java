package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.SendDataRequest;
import com.example.smart_bin_server.model.Log;
import com.example.smart_bin_server.service.LogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {
    private LogService service;

    public LogController(LogService service){
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Object> getLogs(){
        return ResponseEntity.ok().body(service.getLogs());
    }
}
