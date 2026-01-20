package com.example.smart_bin_server.repository;

import com.example.smart_bin_server.model.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(String userId);

    @Query("SELECT n FROM Notification n " +
            "WHERE n.userId = :userId " +
            "AND n.deviceId IS NOT NULL " +
            "AND n.deviceId <> 'system'")
    Page<Notification> findNotificationsByUserIdCustom(@Param("userId") String userId, Pageable pageable);
}
