package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.Notification;
import com.uade.tpo.demo.entity.NotificationType;

public interface NotificationService {

    List<Notification> getAll();

    List<Notification> getUnread();

    Notification markAsRead(Integer id);

    void notify(Integer userId, NotificationType type, String message);
}
