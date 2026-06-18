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
    // Lo completa el controller con el id del seller autenticado; un admin
    // puede mandarlo explícito para crear un producto a nombre de otro seller.
    private int sellerId;
}
