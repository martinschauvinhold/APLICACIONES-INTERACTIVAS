package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Warehouse;

public record WarehouseResponse(
        Integer id,
        String name,
        String location,
        String contactPhone) {

    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getContactPhone());
    }
}
