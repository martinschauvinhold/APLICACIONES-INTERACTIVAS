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

import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;
import com.uade.tpo.demo.service.ProductReturnService;

@RestController
@RequestMapping("returns")
public class ReturnsController {

    @Autowired
    private ProductReturnService productReturnService;

    @GetMapping
    public ResponseEntity<ArrayList<ProductReturn>> getReturns() {
        return ResponseEntity.ok(productReturnService.getReturns());
    }

    @GetMapping("/{returnId}")
    public ResponseEntity<ProductReturn> getReturnById(@PathVariable int returnId) {
        Optional<ProductReturn> result = productReturnService.getReturnById(returnId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ProductReturn>> getReturnsByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(productReturnService.getReturnsByOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<Object> createReturn(@RequestBody ProductReturnRequest returnRequest) {
        ProductReturn result = productReturnService.createReturn(returnRequest);
        return ResponseEntity.created(URI.create("/returns/" + result.getId())).body(result);
    }

    @PutMapping("/{returnId}")
    public ResponseEntity<Object> updateReturn(@PathVariable int returnId, @RequestBody ProductReturnRequest returnRequest) {
        Optional<ProductReturn> result = productReturnService.getReturnById(returnId);
        if (result.isPresent()) {
            ProductReturn updated = productReturnService.updateReturn(returnId, returnRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{returnId}")
    public ResponseEntity<Object> deleteReturn(@PathVariable int returnId) {
        Optional<ProductReturn> result = productReturnService.getReturnById(returnId);
        if (result.isPresent()) {
            productReturnService.deleteReturn(returnId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
