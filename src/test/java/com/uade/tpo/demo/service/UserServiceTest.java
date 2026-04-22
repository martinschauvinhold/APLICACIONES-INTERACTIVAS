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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.UserRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.UserRepository;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUsers_deberiaRetornarListaCompleta() {
        // Arrange
        var users = List.of(
                User.builder().id(1).username("jperez").build(),
                User.builder().id(2).username("mlopez").build());
        when(userRepository.findAll()).thenReturn(users);

        // Act
        var result = userService.getUsers();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getUserById_deberiaRetornarUsuario_cuandoIdExiste() {
        // Arrange
        var user = User.builder().id(1).username("jperez").email("jperez@mail.com").build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        var result = userService.getUserById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jperez");
    }

    @Test
    void getUserById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = userService.getUserById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createUser_deberiaGuardarYRetornarUsuario() {
        // Arrange
        var request = new UserRequest();
        request.setUsername("jperez");
        request.setEmail("jperez@mail.com");
        request.setPassword("password123");
        request.setFirstName("Juan");
        request.setLastName("Pérez");
        request.setRole(Role.buyer);
        request.setPhone("+54 11 1234-5678");

        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hasheado");
        var savedUser = User.builder().id(1).username("jperez").build();
        when(userRepository.save(any())).thenReturn(savedUser);

        // Act
        var result = userService.createUser(request);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any());
    }

    @Test
    void updateUser_deberiaActualizarYRetornarUsuario_cuandoIdExiste() {
        // Arrange
        var user = User.builder().id(1).username("jperez").email("jperez@mail.com").build();
        var request = new UserRequest();
        request.setUsername("jperez_updated");
        request.setEmail("nuevo@mail.com");
        request.setPassword("nuevoPassword123");
        request.setPhone("+54 11 9999-0000");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hasheado");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = userService.updateUser(1, request);

        // Assert
        assertThat(result.getUsername()).isEqualTo("jperez_updated");
        assertThat(result.getEmail()).isEqualTo("nuevo@mail.com");
        verify(passwordEncoder).encode("nuevoPassword123");
    }

    @Test
    void updateUser_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(99, new UserRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteUser_deberiaEliminar() {
        // Act
        userService.deleteUser(1);

        // Assert
        verify(userRepository).deleteById(1);
    }
}
