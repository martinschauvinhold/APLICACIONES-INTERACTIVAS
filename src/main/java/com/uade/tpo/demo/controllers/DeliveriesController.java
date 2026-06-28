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

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;
import com.uade.tpo.demo.entity.dto.DeliveryResponse;
import com.uade.tpo.demo.service.DeliveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("deliveries")
@RequiredArgsConstructor
public class DeliveriesController {

    private final DeliveryService deliveryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveries() {
        List<DeliveryResponse> result = deliveryService.getDeliveries().stream()
                .map(DeliveryResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Integer deliveryId) {
        return ResponseEntity.ok(DeliveryResponse.from(deliveryService.getDeliveryById(deliveryId)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByOrder(@PathVariable Integer orderId) {
        List<DeliveryResponse> result = deliveryService.getDeliveriesByOrder(orderId).stream()
                .map(DeliveryResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<DeliveryResponse> createDelivery(@Valid @RequestBody DeliveryRequest request) {
        Delivery created = deliveryService.createDelivery(request);
        return ResponseEntity.created(URI.create("/deliveries/" + created.getId()))
                .body(DeliveryResponse.from(created));
    }

    @PutMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<DeliveryResponse> updateDelivery(@PathVariable Integer deliveryId,
                                                    @Valid @RequestBody DeliveryRequest request) {
        return ResponseEntity.ok(DeliveryResponse.from(deliveryService.updateDelivery(deliveryId, request)));
    }

    @DeleteMapping("/{deliveryId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Integer deliveryId) {
        deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.noContent().build();
    }
}
