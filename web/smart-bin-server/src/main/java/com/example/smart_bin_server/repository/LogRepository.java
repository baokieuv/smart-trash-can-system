package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

}
