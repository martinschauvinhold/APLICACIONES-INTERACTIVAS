package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductVariantRequest {
    private int productId;
    private String sku;
    private String attributes;
    private BigDecimal basePrice;
}
