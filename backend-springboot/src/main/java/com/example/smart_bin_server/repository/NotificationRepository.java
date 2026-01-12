package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
