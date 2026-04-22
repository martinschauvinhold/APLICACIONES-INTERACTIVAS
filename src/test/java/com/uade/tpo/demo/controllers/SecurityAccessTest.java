package com.uade.tpo.demo.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void endpointProtegido_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "buyer")
    void endpointAdmin_deberiaRetornar403_cuandoRolEsBuyer() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "admin")
    void endpointAdmin_deberiaNoRetornar401Ni403_cuandoRolEsAdmin() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    // ── Endpoints públicos de catálogo: deben responder sin token ─────────────

    @Test
    void getCategorias_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getCategoriaPorId_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getProductos_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getProductoPorId_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getVariantes_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/variants"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getVariantePorId_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/variants/1"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getVariantesPorProducto_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/variants/product/1"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getReviews_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getReviewPorId_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    @Test
    void getReviewsPorProducto_deberiaSerPublico_sinToken() throws Exception {
        mockMvc.perform(get("/reviews/product/1"))
                .andExpect(status().is(not(401)))
                .andExpect(status().is(not(403)));
    }

    // ── Las mutaciones sobre los mismos recursos siguen requiriendo token ─────

    @Test
    void postCategoria_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(post("/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postProducto_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(post("/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postVariante_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(post("/variants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postReview_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(post("/reviews"))
                .andExpect(status().isUnauthorized());
    }

    // ── Endpoints sensibles que NO deben volverse públicos por error ──────────

    @Test
    void getCoupons_deberiaSeguirRequierendo401_sinToken() throws Exception {
        mockMvc.perform(get("/coupons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDeliveries_deberiaSeguirRequierendo401_sinToken() throws Exception {
        mockMvc.perform(get("/deliveries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTracking_deberiaSeguirRequierendo401_sinToken() throws Exception {
        mockMvc.perform(get("/tracking/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSupportTicket_deberiaSeguirRequierendo401_sinToken() throws Exception {
        mockMvc.perform(get("/support/tickets/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── Discounts: escritura solo admin (regresión TESTING.md #46) ────────────

    @Test
    void postDiscount_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(post("/discounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putDiscount_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(put("/discounts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteDiscount_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(delete("/discounts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "buyer")
    void postDiscount_deberiaRetornar403_cuandoRolEsBuyer() throws Exception {
        mockMvc.perform(post("/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "seller")
    void postDiscount_deberiaRetornar403_cuandoRolEsSeller() throws Exception {
        mockMvc.perform(post("/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ── Addresses: escritura requiere autenticación (regresión TESTING.md #47) ─

    @Test
    void postAddress_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(post("/addresses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putAddress_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(put("/addresses/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAddress_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(delete("/addresses/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── Sessions: el controller ya no se expone (regresión TESTING.md #48) ────

    @Test
    void getSessionsByUser_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(get("/sessions/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteSession_deberiaRetornar401_cuandoSinToken() throws Exception {
        mockMvc.perform(delete("/sessions/1"))
                .andExpect(status().isUnauthorized());
    }
}
