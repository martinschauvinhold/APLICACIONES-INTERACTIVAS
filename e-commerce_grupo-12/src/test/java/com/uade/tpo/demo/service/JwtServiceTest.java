package com.uade.tpo.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void generateToken_deberiaRetornarTokenNoNulo_cuandoEmailValido() {
        String token = jwtService.generateToken("juan@mail.com");

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractEmail_deberiaRetornarEmail_cuandoTokenValido() {
        String token = jwtService.generateToken("juan@mail.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("juan@mail.com");
    }

    @Test
    void isTokenValid_deberiaRetornarTrue_cuandoTokenValido() {
        String token = jwtService.generateToken("juan@mail.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_deberiaRetornarFalse_cuandoTokenMalformado() {
        assertThat(jwtService.isTokenValid("esto.no.es.un.token")).isFalse();
    }

    @Test
    void isTokenValid_deberiaRetornarFalse_cuandoTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String tokenExpirado = jwtService.generateToken("juan@mail.com");

        assertThat(jwtService.isTokenValid(tokenExpirado)).isFalse();
    }

    @Test
    void getExpirationDate_deberiaRetornarFechaFutura() {
        Date expiracion = jwtService.getExpirationDate();

        assertThat(expiracion).isAfter(new Date());
    }
}
