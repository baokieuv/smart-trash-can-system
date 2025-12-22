package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.LogDto;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.Log;
import com.example.smart_bin_server.repository.DeviceRepository;
import com.example.smart_bin_server.repository.LogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {
    private final LogRepository repository;
    private final DeviceRepository deviceRepository;

    public LogService (LogRepository repository, DeviceRepository deviceRepository){
        this.repository = repository;
        this.deviceRepository = deviceRepository;
    }

    public LogDto addLog(Log log){
        return parseToDto(repository.save(log));
    }

    public List<LogDto> getLogs() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by(Sort.Direction.DESC, "id")
        );
        return repository.findAll(pageable).getContent()
                .stream()
                .map(this::parseToDto)
                .collect(Collectors.toList());
    }

    private LogDto parseToDto(Log log){
        Device device = deviceRepository.findById(log.getDeviceId()).orElse(null);
        String deviceName = device == null ? "Unknown device" : device.getName();
        return new LogDto(log.getId(), deviceName, log.getMessage(), log.getType().toString(), log.getTimestamp());
    }
}
