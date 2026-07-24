package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Product;

public record ProductResponse(
        Integer id,
        String name,
        String description,
        String brand,
        boolean active,
        Integer categoryId,
        String categoryName,
        Integer sellerId,
        String sellerName,
        String imageUrl,
        Date updatedAt) {

    /**
     * Resumen del producto para el listado público. Expone solo un resumen del
     * vendedor (id + username) para no filtrar PII del User (email, teléfono).
     * La imagen primaria la resuelve el service (queda null si no se conoce).
     */
    public static ProductResponse from(Product product) {
        return from(product, null);
    }

    public static ProductResponse from(Product product, String imageUrl) {
        var category = product.getCategory();
        var seller = product.getSeller();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.isActive(),
                category != null ? category.getId() : null,
                category != null ? category.getDescription() : null,
                seller != null ? seller.getId() : null,
                seller != null ? seller.getUsername() : null,
                imageUrl,
                product.getUpdatedAt());
    }
}
