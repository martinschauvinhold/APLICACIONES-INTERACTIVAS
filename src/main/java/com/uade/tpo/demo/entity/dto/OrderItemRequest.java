package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    private int variantId;
    @Positive private int quantity;
}
