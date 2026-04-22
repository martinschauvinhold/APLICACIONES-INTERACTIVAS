package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
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

import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.service.DiscountService;

@RestController
@RequestMapping("discounts")
public class DiscountsController {

    @Autowired
    private DiscountService discountService;

    @GetMapping
    public ResponseEntity<List<Discount>> getDiscounts() {
        return ResponseEntity.ok(discountService.getDiscounts());
    }

    @GetMapping("/{discountId}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable int discountId) {
        Optional<Discount> result = discountService.getDiscountById(discountId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createDiscount(@RequestBody DiscountRequest request) {
        Discount result = discountService.createDiscount(request);
        return ResponseEntity.created(URI.create("/discounts/" + result.getId())).body(result);
    }

    @PutMapping("/{discountId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> updateDiscount(@PathVariable int discountId,
            @RequestBody DiscountRequest request) {
        Discount updated = discountService.updateDiscount(discountId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteDiscount(@PathVariable int discountId) {
        discountService.deleteDiscount(discountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('buyer', 'seller', 'admin')")
    public ResponseEntity<List<Discount>> getActiveDiscountsForProduct(@PathVariable int productId) {
        return ResponseEntity.ok(discountService.getActiveDiscountsForProduct(productId));
    }
}
