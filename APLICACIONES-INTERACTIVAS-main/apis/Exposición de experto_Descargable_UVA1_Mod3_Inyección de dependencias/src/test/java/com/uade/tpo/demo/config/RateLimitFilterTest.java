package com.uade.tpo.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitFilterTest {

    @RestController
    static class TestController {
        @GetMapping("/auth/login")
        public String authEndpoint() { return "ok"; }

        @GetMapping("/products")
        public String generalEndpoint() { return "ok"; }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RateLimitFilter filter = new RateLimitFilter();
        ReflectionTestUtils.setField(filter, "authCapacity", 2);
        ReflectionTestUtils.setField(filter, "generalCapacity", 3);
        ReflectionTestUtils.setField(filter, "objectMapper",
                new ObjectMapper().registerModule(new JavaTimeModule()));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .addFilter(filter)
                .build();
    }

    @Test
    void deberiaPermitirSolicitudes_cuandoNoBajoElLimite() throws Exception {
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
        mockMvc.perform(get("/products")).andExpect(status().isOk());
    }

    @Test
    void deberiaRetornar429_cuandoSeSuperaLimiteDeAuth() throws Exception {
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());

        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deberiaRetornar429_cuandoSeSuperaLimiteGeneral() throws Exception {
        mockMvc.perform(get("/products")).andExpect(status().isOk());
        mockMvc.perform(get("/products")).andExpect(status().isOk());
        mockMvc.perform(get("/products")).andExpect(status().isOk());

        mockMvc.perform(get("/products")).andExpect(status().isTooManyRequests());
    }

    @Test
    void deberiaAplicarLimitesIndependientes_paraCadaTipoDeEndpoint() throws Exception {
        // Agota el límite de auth (2 requests)
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/login")).andExpect(status().isTooManyRequests());

        // El límite general no se vio afectado
        mockMvc.perform(get("/products")).andExpect(status().isOk());
    }
}
