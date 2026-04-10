package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Optional;

import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.entity.dto.WarehouseRequest;

public interface WarehouseService {
    public ArrayList<Warehouse> getWarehouses();

    public Optional<Warehouse> getWarehouseById(int warehouseId);

    public Warehouse createWarehouse(WarehouseRequest warehouseRequest);

    public Warehouse updateWarehouse(int warehouseId, WarehouseRequest warehouseRequest);

    public void deleteWarehouse(int warehouseId);
}
