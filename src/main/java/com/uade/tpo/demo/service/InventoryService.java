package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.dto.InventoryRequest;

public interface InventoryService {
    public ArrayList<Inventory> getInventory();

    public Optional<Inventory> getInventoryById(int inventoryId);

    public List<Inventory> getInventoryByVariant(int variantId);

    public Inventory createInventory(InventoryRequest inventoryRequest);

    public Inventory updateInventory(int inventoryId, InventoryRequest inventoryRequest);

    public void deleteInventory(int inventoryId);
}
