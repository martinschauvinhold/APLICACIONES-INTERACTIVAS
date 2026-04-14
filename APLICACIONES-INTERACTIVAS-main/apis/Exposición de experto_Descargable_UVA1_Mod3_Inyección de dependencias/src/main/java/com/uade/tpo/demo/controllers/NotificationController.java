package com.uade.tpo.demo.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Notification;
import com.uade.tpo.demo.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnread() {
        return ResponseEntity.ok(notificationService.getUnread());
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
}
