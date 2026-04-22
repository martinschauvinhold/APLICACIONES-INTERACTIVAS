package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Notification;
import com.uade.tpo.demo.entity.NotificationType;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.NotificationRepository;
import com.uade.tpo.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getAll_deberiaRetornarTodasLasNotificaciones() {
        // Arrange
        var notifications = List.of(
                Notification.builder().id(1).type(NotificationType.ORDER_DISPATCHED).message("Tu pedido fue despachado").build(),
                Notification.builder().id(2).type(NotificationType.REFUND_PROCESSED).message("Tu reembolso fue procesado").build());
        when(notificationRepository.findAll()).thenReturn(notifications);

        // Act
        var result = notificationService.getAll();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getUnread_deberiaRetornarSoloLasNoLeidas() {
        // Arrange
        var unread = List.of(
                Notification.builder().id(1).isRead(false).type(NotificationType.TICKET_CLOSED).message("Tu ticket fue cerrado").build());
        when(notificationRepository.findByIsRead(false)).thenReturn(unread);

        // Act
        var result = notificationService.getUnread();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    void markAsRead_deberiaMarcarComoLeida_cuandoIdExiste() {
        // Arrange
        var notification = Notification.builder().id(1).isRead(false).type(NotificationType.GENERIC).build();
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = notificationService.markAsRead(1);

        // Assert
        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).save(any());
    }

    @Test
    void markAsRead_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(notificationRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.markAsRead(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void notify_deberiaGuardarNotificacion_cuandoUserExiste() {
        // Arrange
        var user = User.builder().id(1).username("john").build();
        var saved = Notification.builder().id(10).user(user)
                .type(NotificationType.ORDER_DISPATCHED).message("Tu pedido fue despachado").isRead(false).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any())).thenReturn(saved);

        // Act
        notificationService.notify(1, NotificationType.ORDER_DISPATCHED, "Tu pedido fue despachado");

        // Assert
        verify(notificationRepository).save(any());
    }

    @Test
    void notify_deberiaLanzarNotFoundException_cuandoUserNoExiste() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.notify(99, NotificationType.GENERIC, "Hola"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("99");
    }
}
