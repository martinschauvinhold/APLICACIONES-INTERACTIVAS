package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.InventoryRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.InventoryService;
import com.uade.tpo.demo.service.ProductVariantService;

@RestController
@RequestMapping("inventory")
@PreAuthorize("hasAnyRole('seller', 'admin')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<ArrayList<Inventory>> getInventory() {
        return ResponseEntity.ok(inventoryService.getInventory());
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable int inventoryId) {
        Optional<Inventory> result = inventoryService.getInventoryById(inventoryId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        requireOwnerOrAdmin(result.get().getVariant().getProduct());
        return ResponseEntity.ok(result.get());
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<Inventory>> getInventoryByVariant(@PathVariable int variantId) {
        ProductVariant variant = productVariantService.getVariantById(variantId)
                .orElseThrow(() -> new NotFoundException("ProductVariant", variantId));
        requireOwnerOrAdmin(variant.getProduct());
        return ResponseEntity.ok(inventoryService.getInventoryByVariant(variantId));
    }

    @PostMapping
    public ResponseEntity<Object> createInventory(@RequestBody InventoryRequest inventoryRequest) {
        ProductVariant variant = productVariantService.getVariantById(inventoryRequest.getVariantId())
                .orElseThrow(() -> new NotFoundException("ProductVariant", inventoryRequest.getVariantId()));
        requireOwnerOrAdmin(variant.getProduct());
        Inventory result = inventoryService.createInventory(inventoryRequest);
        return ResponseEntity.created(URI.create("/inventory/" + result.getId())).body(result);
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<Object> updateInventory(@PathVariable int inventoryId, @RequestBody InventoryRequest inventoryRequest) {
        Optional<Inventory> result = inventoryService.getInventoryById(inventoryId);
        if (result.isPresent()) {
            requireOwnerOrAdmin(result.get().getVariant().getProduct());
            Inventory updated = inventoryService.updateInventory(inventoryId, inventoryRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Object> deleteInventory(@PathVariable int inventoryId) {
        Optional<Inventory> result = inventoryService.getInventoryById(inventoryId);
        if (result.isPresent()) {
            requireOwnerOrAdmin(result.get().getVariant().getProduct());
            inventoryService.deleteInventory(inventoryId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void requireOwnerOrAdmin(Product product) {
        if (product.getSeller() == null) {
            if (!authorizationService.isAdmin()) {
                throw new AccessDeniedException("Este producto no tiene vendedor asignado; solo un admin puede editarlo");
            }
            return;
        }
        authorizationService.requireSelfOrAdmin(product.getSeller().getId());
    }
}
