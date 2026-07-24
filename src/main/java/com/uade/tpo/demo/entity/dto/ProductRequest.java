package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank private String name;
    private String description;
    private String brand;
    @Positive private int categoryId;
    // Lo completa el controller con el id del vendedor autenticado; el valor que
    // mande el cliente se ignora (un producto siempre pertenece a su vendedor).
    private int sellerId;
}
