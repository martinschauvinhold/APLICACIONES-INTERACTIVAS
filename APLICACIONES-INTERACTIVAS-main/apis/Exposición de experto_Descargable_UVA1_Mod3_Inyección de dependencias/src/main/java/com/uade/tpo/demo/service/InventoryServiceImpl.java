package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.entity.dto.InventoryRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;
import com.uade.tpo.demo.repository.WarehouseRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    public ArrayList<Inventory> getInventory() {
        return new ArrayList<>(inventoryRepository.findAll());
    }

    public Optional<Inventory> getInventoryById(int inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }

    public List<Inventory> getInventoryByVariant(int variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new NotFoundException("ProductVariant", variantId);
        }
        return inventoryRepository.findByVariantId(variantId);
    }

    public Inventory createInventory(InventoryRequest inventoryRequest) {
        ProductVariant variant = productVariantRepository.findById(inventoryRequest.getVariantId())
                .orElseThrow(() -> new NotFoundException("ProductVariant", inventoryRequest.getVariantId()));
        Warehouse warehouse = warehouseRepository.findById(inventoryRequest.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse", inventoryRequest.getWarehouseId()));
        Inventory inventory = Inventory.builder()
                .variant(variant)
                .warehouse(warehouse)
                .stockQuantity(inventoryRequest.getStockQuantity())
                .lastUpdated(new Date())
                .build();
        return inventoryRepository.save(inventory);
    }

    public Inventory updateInventory(int inventoryId, InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new NotFoundException("Inventory", inventoryId));
        inventory.setStockQuantity(inventoryRequest.getStockQuantity());
        inventory.setLastUpdated(new Date());
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(int inventoryId) {
        inventoryRepository.deleteById(inventoryId);
    }
}
