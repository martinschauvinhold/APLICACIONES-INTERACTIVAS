package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;
import com.uade.tpo.demo.service.ProductVariantService;

@RestController
@RequestMapping("variants")
public class ProductVariantsController {

    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<ArrayList<ProductVariant>> getVariants() {
        return ResponseEntity.ok(productVariantService.getVariants());
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ProductVariant> getVariantById(@PathVariable int variantId) {
        Optional<ProductVariant> result = productVariantService.getVariantById(variantId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariant>> getVariantsByProduct(@PathVariable int productId) {
        return ResponseEntity.ok(productVariantService.getVariantsByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<Object> createVariant(@RequestBody ProductVariantRequest variantRequest) {
        ProductVariant result = productVariantService.createVariant(variantRequest);
        return ResponseEntity.created(URI.create("/variants/" + result.getId())).body(result);
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<Object> updateVariant(@PathVariable int variantId, @RequestBody ProductVariantRequest variantRequest) {
        Optional<ProductVariant> result = productVariantService.getVariantById(variantId);
        if (result.isPresent()) {
            ProductVariant updated = productVariantService.updateVariant(variantId, variantRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Object> deleteVariant(@PathVariable int variantId) {
        Optional<ProductVariant> result = productVariantService.getVariantById(variantId);
        if (result.isPresent()) {
            productVariantService.deleteVariant(variantId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
