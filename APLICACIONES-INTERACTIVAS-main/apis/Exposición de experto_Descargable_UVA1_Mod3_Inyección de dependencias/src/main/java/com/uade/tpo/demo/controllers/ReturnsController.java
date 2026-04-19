package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

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

import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;
import com.uade.tpo.demo.service.ProductReturnService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("returns")
@RequiredArgsConstructor
public class ReturnsController {

    private final ProductReturnService productReturnService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<ProductReturn>> getReturns() {
        return ResponseEntity.ok(productReturnService.getReturns());
    }

    @GetMapping("/{returnId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<ProductReturn> getReturnById(@PathVariable Integer returnId) {
        return ResponseEntity.ok(productReturnService.getReturnById(returnId));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<ProductReturn>> getReturnsByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(productReturnService.getReturnsByOrder(orderId));
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<ProductReturn> createReturn(@Valid @RequestBody ProductReturnRequest request) {
        ProductReturn created = productReturnService.createReturn(request);
        return ResponseEntity.created(URI.create("/returns/" + created.getId())).body(created);
    }

    @PutMapping("/{returnId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ProductReturn> updateReturn(@PathVariable Integer returnId,
                                                       @Valid @RequestBody ProductReturnRequest request) {
        return ResponseEntity.ok(productReturnService.updateReturn(returnId, request));
    }

    @DeleteMapping("/{returnId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteReturn(@PathVariable Integer returnId) {
        productReturnService.deleteReturn(returnId);
        return ResponseEntity.noContent().build();
    }
}
