package com.uade.tpo.demo.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    // Controller mínimo solo para disparar las excepciones en los tests
    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void notFound() {
            throw new NotFoundException("Delivery", 99);
        }

        @GetMapping("/test/duplicate")
        public void duplicate() {
            throw new DuplicateException("Category", "description", "Laptops");
        }

        @GetMapping("/test/business-rule")
        public void businessRule() {
            throw new BusinessRuleException("No se puede cancelar un pedido ya enviado");
        }
    }

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deberiaRetornar404_cuandoLanzaNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Delivery con id 99 no encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deberiaRetornar409_cuandoLanzaDuplicateException() throws Exception {
        mockMvc.perform(get("/test/duplicate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Category con description 'Laptops' ya existe"));
    }

    @Test
    void deberiaRetornar422_cuandoLanzaBusinessRuleException() throws Exception {
        mockMvc.perform(get("/test/business-rule").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("No se puede cancelar un pedido ya enviado"));
    }
}
