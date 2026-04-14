package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;
import com.uade.tpo.demo.service.DeliveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("deliveries")
@RequiredArgsConstructor
public class DeliveriesController {

    private final DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<List<Delivery>> getDeliveries() {
        return ResponseEntity.ok(deliveryService.getDeliveries());
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Integer deliveryId) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(deliveryId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Delivery>> getDeliveriesByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<Delivery> createDelivery(@Valid @RequestBody DeliveryRequest request) {
        Delivery created = deliveryService.createDelivery(request);
        return ResponseEntity.created(URI.create("/deliveries/" + created.getId())).body(created);
    }

    @PutMapping("/{deliveryId}")
    public ResponseEntity<Delivery> updateDelivery(@PathVariable Integer deliveryId,
                                                    @Valid @RequestBody DeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.updateDelivery(deliveryId, request));
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Integer deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.noContent().build();
    }
}
