package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.uade.tpo.demo.entity.ProductVariant;

public record ProductVariantResponse(
        Integer id,
        Integer productId,
        String productName,
        String sku,
        String attributes,
        BigDecimal basePrice,
        Date updatedAt) {

    public static ProductVariantResponse from(ProductVariant variant) {
        var product = variant.getProduct();
        return new ProductVariantResponse(
                variant.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                variant.getSku(),
                variant.getAttributes(),
                variant.getBasePrice(),
                variant.getUpdatedAt());
    }
}
