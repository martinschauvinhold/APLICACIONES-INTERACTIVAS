package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class InventoryRequest {
    private int variantId;
    private int warehouseId;
    private int stockQuantity;
}
