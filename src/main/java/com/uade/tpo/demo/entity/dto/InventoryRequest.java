package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class InventoryRequest {
    private int variantId;
    private int warehouseId;
    @PositiveOrZero private int stockQuantity;
}
