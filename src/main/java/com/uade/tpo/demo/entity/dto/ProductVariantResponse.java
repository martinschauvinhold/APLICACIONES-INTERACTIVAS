package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.uade.tpo.demo.entity.ProductVariant;

public record ProductVariantResponse(
        Integer id,
        Integer productId,
        String productName,
        String sku,
        String attributes,
        BigDecimal basePrice,
        Date updatedAt,
        Integer stock,
        List<PriceTierResponse> tiers) {

    /** Variante sola, sin stock/tiers embebidos (para altas/bajas puntuales). */
    public static ProductVariantResponse from(ProductVariant variant) {
        return from(variant, null, null);
    }

    /** Variante con stock/tiers ya calculados en lote (para listados: ver ProductVariantServiceImpl.hydrate). */
    public static ProductVariantResponse from(ProductVariant variant, Integer stock, List<PriceTierResponse> tiers) {
        var product = variant.getProduct();
        return new ProductVariantResponse(
                variant.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                variant.getSku(),
                variant.getAttributes(),
                variant.getBasePrice(),
                variant.getUpdatedAt(),
                stock,
                tiers);
    }
}
