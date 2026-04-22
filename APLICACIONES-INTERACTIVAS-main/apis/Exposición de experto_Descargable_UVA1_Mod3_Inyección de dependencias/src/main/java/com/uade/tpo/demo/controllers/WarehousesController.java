package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.entity.dto.WarehouseRequest;
import com.uade.tpo.demo.service.WarehouseService;

@RestController
@RequestMapping("warehouses")
public class WarehousesController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<ArrayList<Warehouse>> getWarehouses() {
        return ResponseEntity.ok(warehouseService.getWarehouses());
    }

    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable int warehouseId) {
        Optional<Warehouse> result = warehouseService.getWarehouseById(warehouseId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createWarehouse(@RequestBody WarehouseRequest warehouseRequest) {
        Warehouse result = warehouseService.createWarehouse(warehouseRequest);
        return ResponseEntity.created(URI.create("/warehouses/" + result.getId())).body(result);
    }

    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> updateWarehouse(@PathVariable int warehouseId, @RequestBody WarehouseRequest warehouseRequest) {
        Optional<Warehouse> result = warehouseService.getWarehouseById(warehouseId);
        if (result.isPresent()) {
            Warehouse updated = warehouseService.updateWarehouse(warehouseId, warehouseRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteWarehouse(@PathVariable int warehouseId) {
        Optional<Warehouse> result = warehouseService.getWarehouseById(warehouseId);
        if (result.isPresent()) {
            warehouseService.deleteWarehouse(warehouseId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
