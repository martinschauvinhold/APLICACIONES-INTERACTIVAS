package com.uade.tpo.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.Session;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.repository.SessionRepository;
import com.uade.tpo.demo.repository.UserRepository;
import com.uade.tpo.demo.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationManager authenticationManager;

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_deberiaRetornar200ConToken_cuandoDatosValidos() throws Exception {
        when(userRepository.findByEmail("juan@mail.com")).thenReturn(Optional.empty());
        when(jwtService.generateToken("juan@mail.com")).thenReturn("jwt.token.generado");

        String body = """
                {
                  "username": "jperez",
                  "email": "juan@mail.com",
                  "password": "Samsung123!",
                  "firstName": "Juan",
                  "lastName": "Pérez",
                  "phone": "+54 11 1234-5678"
                }
                """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.generado"));
    }

    @Test
    void register_deberiaRetornar409_cuandoEmailYaExiste() throws Exception {
        var existente = User.builder().id(1).email("juan@mail.com").build();
        when(userRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(existente));

        String body = """
                {
                  "username": "jperez",
                  "email": "juan@mail.com",
                  "password": "Samsung123!",
                  "firstName": "Juan",
                  "lastName": "Pérez",
                  "phone": "+54 11 1234-5678"
                }
                """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_deberiaRetornar200ConToken_cuandoCredencialesCorrectas() throws Exception {
        var usuario = User.builder()
                .id(1).email("juan@mail.com").passwordHash("hashed").role(Role.buyer)
                .build();
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken("juan@mail.com")).thenReturn("jwt.token.generado");
        when(jwtService.getExpirationDate()).thenReturn(new Date());

        String body = """
                {
                  "email": "juan@mail.com",
                  "password": "Samsung123!"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.generado"));

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void login_deberiaRetornar401_cuandoCredencialesInvalidas() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        String body = """
                {
                  "email": "juan@mail.com",
                  "password": "contraseña_incorrecta"
                }
                """;

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "juan@mail.com")
    void logout_deberiaRetornar204YEliminarSesion_cuandoUsuarioAutenticado() throws Exception {
        var usuario = User.builder().id(1).email("juan@mail.com").role(Role.buyer).build();
        when(userRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent());

        verify(sessionRepository).deleteByUser(usuario);
    }

    @Test
    void logout_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // ── JwtAuthFilter: blacklist de sesión ────────────────────────────────────

    @Test
    void jwtFilter_deberiaRetornar401_cuandoTokenValidoPeroSesionEliminada() throws Exception {
        when(jwtService.extractEmail(anyString())).thenReturn("juan@mail.com");
        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(sessionRepository.existsByUserEmail("juan@mail.com")).thenReturn(false);

        var usuario = User.builder().id(1).email("juan@mail.com").passwordHash("hashed").role(Role.buyer).build();
        when(userRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer token.post.logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtFilter_deberiaAutenticar_cuandoTokenValidoYSesionActiva() throws Exception {
        when(jwtService.extractEmail(anyString())).thenReturn("juan@mail.com");
        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(sessionRepository.existsByUserEmail("juan@mail.com")).thenReturn(true);

        var usuario = User.builder().id(1).email("juan@mail.com").passwordHash("hashed").role(Role.buyer).build();
        when(userRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer token.activo"))
                .andExpect(status().isNoContent());
    }
}
