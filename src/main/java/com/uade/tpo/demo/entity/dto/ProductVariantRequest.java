package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductVariantRequest {
    private int productId;
    private String sku;
    private String attributes;
    @NotNull @Positive private BigDecimal basePrice;
}
