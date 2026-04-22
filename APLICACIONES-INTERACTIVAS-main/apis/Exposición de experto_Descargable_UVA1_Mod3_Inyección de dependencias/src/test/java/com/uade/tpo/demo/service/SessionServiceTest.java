package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Session;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void getSessionsByUser_deberiaRetornarSesionesDelUsuario() {
        // Arrange
        var user = User.builder().id(5).build();
        var sessions = List.of(
                Session.builder().id(1).user(user).deviceInfo("Chrome").build(),
                Session.builder().id(2).user(user).deviceInfo("Firefox").build());
        when(sessionRepository.findByUserId(5)).thenReturn(sessions);

        // Act
        var result = sessionService.getSessionsByUser(5);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getSessionById_deberiaRetornarSesion_cuandoIdExiste() {
        // Arrange
        var user = User.builder().id(5).build();
        var session = Session.builder().id(1).user(user).deviceInfo("Chrome").build();
        when(sessionRepository.findById(1)).thenReturn(Optional.of(session));

        // Act
        var result = sessionService.getSessionById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDeviceInfo()).isEqualTo("Chrome");
    }

    @Test
    void getSessionById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(sessionRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = sessionService.getSessionById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createSession_deberiaGuardarYRetornarSesion() {
        // Arrange
        var user = User.builder().id(5).build();
        var session = Session.builder().user(user).ipAddress("192.168.1.1").expiresAt(new Date()).build();
        var saved = Session.builder().id(10).user(user).ipAddress("192.168.1.1").build();
        when(sessionRepository.save(any())).thenReturn(saved);

        // Act
        var result = sessionService.createSession(session);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        verify(sessionRepository).save(session);
    }

    @Test
    void deleteSession_deberiaEliminar() {
        // Act
        sessionService.deleteSession(1);

        // Assert
        verify(sessionRepository).deleteById(1);
    }
}
