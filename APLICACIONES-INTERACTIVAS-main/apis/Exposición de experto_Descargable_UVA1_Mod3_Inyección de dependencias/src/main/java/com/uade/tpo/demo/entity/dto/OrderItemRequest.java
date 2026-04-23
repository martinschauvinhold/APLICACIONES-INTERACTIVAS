package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private int variantId;
    private int quantity;
}
