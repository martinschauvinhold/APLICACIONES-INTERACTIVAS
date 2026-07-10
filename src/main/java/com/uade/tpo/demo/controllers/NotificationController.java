package com.uade.tpo.demo.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Notification;
import com.uade.tpo.demo.entity.dto.NotificationResponse;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        List<NotificationResponse> result = notificationService.getAll().stream().map(NotificationResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread() {
        Integer userId = authorizationService.currentUser().getId();
        List<NotificationResponse> result = notificationService.getUnreadByUser(userId).stream()
                .map(NotificationResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Integer notificationId) {
        Notification notification = notificationService.getById(notificationId);
        authorizationService.requireSelfOrAdmin(notification.getUser().getId());
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markAsRead(notificationId)));
    }
}
