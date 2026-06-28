package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Notification;
import com.uade.tpo.demo.entity.NotificationType;

public record NotificationResponse(
        Integer id,
        Integer userId,
        NotificationType type,
        String message,
        boolean isRead,
        Date createdAt) {

    public static NotificationResponse from(Notification notification) {
        var user = notification.getUser();
        return new NotificationResponse(
                notification.getId(),
                user != null ? user.getId() : null,
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
