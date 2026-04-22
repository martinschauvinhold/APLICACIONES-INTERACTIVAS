package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Notification;
import com.uade.tpo.demo.entity.NotificationType;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.NotificationRepository;
import com.uade.tpo.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getUnread() {
        return notificationRepository.findByIsRead(false);
    }

    @Override
    @Transactional
    public Notification markAsRead(Integer id) {
        var notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification", id));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notify(Integer userId, NotificationType type, String message) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        var notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .isRead(false)
                .createdAt(new Date())
                .build();

        notificationRepository.save(notification);
    }
}
