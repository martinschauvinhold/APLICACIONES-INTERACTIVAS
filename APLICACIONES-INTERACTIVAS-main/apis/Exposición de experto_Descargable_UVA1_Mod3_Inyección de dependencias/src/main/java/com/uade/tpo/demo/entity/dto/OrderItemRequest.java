package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemRequest {
    private int orderId;
    private int variantId;
    private int quantity;
    private BigDecimal unitPriceAtTime;
    private BigDecimal discountApplied;
    private BigDecimal subtotal;
}
