package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import com.uade.tpo.demo.entity.OrderItem;

public record OrderItemResponse(
        Integer id,
        Integer orderId,
        Integer variantId,
        String variantSku,
        String productName,
        String variantAttributes,
        BigDecimal basePrice,
        int quantity,
        BigDecimal unitPriceAtTime,
        BigDecimal discountApplied,
        BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem orderItem) {
        var order = orderItem.getOrder();
        var variant = orderItem.getVariant();
        var product = variant != null ? variant.getProduct() : null;
        return new OrderItemResponse(
                orderItem.getId(),
                order != null ? order.getId() : null,
                variant != null ? variant.getId() : null,
                variant != null ? variant.getSku() : null,
                product != null ? product.getName() : null,
                variant != null ? variant.getAttributes() : null,
                variant != null ? variant.getBasePrice() : null,
                orderItem.getQuantity(),
                orderItem.getUnitPriceAtTime(),
                orderItem.getDiscountApplied(),
                orderItem.getSubtotal());
    }
}
