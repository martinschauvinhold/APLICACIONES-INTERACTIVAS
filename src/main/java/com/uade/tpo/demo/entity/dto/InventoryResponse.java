package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Inventory;

public record InventoryResponse(
        Integer id,
        Integer variantId,
        String variantSku,
        Integer warehouseId,
        String warehouseName,
        int stockQuantity,
        Date lastUpdated) {

    public static InventoryResponse from(Inventory inventory) {
        var variant = inventory.getVariant();
        var warehouse = inventory.getWarehouse();
        return new InventoryResponse(
                inventory.getId(),
                variant != null ? variant.getId() : null,
                variant != null ? variant.getSku() : null,
                warehouse != null ? warehouse.getId() : null,
                warehouse != null ? warehouse.getName() : null,
                inventory.getStockQuantity(),
                inventory.getLastUpdated());
    }
}
