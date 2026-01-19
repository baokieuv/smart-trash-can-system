package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.ClassificationLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassificationLogsRepository extends JpaRepository<ClassificationLogs, Long> {
}
