package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.uade.tpo.demo.entity.Discount;

public record DiscountResponse(
        Integer id,
        String name,
        String discountType,
        BigDecimal value,
        String appliesTo,
        Integer productId,
        String productName,
        Integer categoryId,
        String categoryName,
        BigDecimal minPrice,
        Date startsAt,
        Date expiresAt,
        boolean isActive) {

    public static DiscountResponse from(Discount discount) {
        var product = discount.getProduct();
        var category = discount.getCategory();
        return new DiscountResponse(
                discount.getId(),
                discount.getName(),
                discount.getDiscountType(),
                discount.getValue(),
                discount.getAppliesTo(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                category != null ? category.getId() : null,
                category != null ? category.getDescription() : null,
                discount.getMinPrice(),
                discount.getStartsAt(),
                discount.getExpiresAt(),
                discount.isActive());
    }
}
