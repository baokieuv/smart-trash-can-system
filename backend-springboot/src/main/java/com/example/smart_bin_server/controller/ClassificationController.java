package com.example.smart_bin_server.controller;

import com.example.smart_bin_server.dto.ClassificationResponse;
import com.example.smart_bin_server.service.ClassificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/classify-image")
public class ClassificationController {

    private final ClassificationService service;

    public ClassificationController(ClassificationService service){
        this.service = service;
    }

//    @PostMapping
//    public ResponseEntity<Object> classify(@RequestBody MultipartFile image){
//        return ResponseEntity.ok().body(service.classify(image));
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> classify(
            @RequestPart("image") MultipartFile image,
            @RequestParam("deviceId") String deviceId){
        return ResponseEntity.ok().body(service.classify(image, deviceId));
    }
}
