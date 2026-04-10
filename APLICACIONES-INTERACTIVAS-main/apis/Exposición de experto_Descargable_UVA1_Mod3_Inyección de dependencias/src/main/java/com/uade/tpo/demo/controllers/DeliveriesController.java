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

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;
import com.uade.tpo.demo.service.DeliveryService;

@RestController
@RequestMapping("deliveries")
public class DeliveriesController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<ArrayList<Delivery>> getDeliveries() {
        return ResponseEntity.ok(deliveryService.getDeliveries());
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable int deliveryId) {
        Optional<Delivery> result = deliveryService.getDeliveryById(deliveryId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Delivery>> getDeliveriesByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<Object> createDelivery(@RequestBody DeliveryRequest deliveryRequest) {
        Delivery result = deliveryService.createDelivery(deliveryRequest);
        return ResponseEntity.created(URI.create("/deliveries/" + result.getId())).body(result);
    }

    @PutMapping("/{deliveryId}")
    public ResponseEntity<Object> updateDelivery(@PathVariable int deliveryId, @RequestBody DeliveryRequest deliveryRequest) {
        Optional<Delivery> result = deliveryService.getDeliveryById(deliveryId);
        if (result.isPresent()) {
            Delivery updated = deliveryService.updateDelivery(deliveryId, deliveryRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Object> deleteDelivery(@PathVariable int deliveryId) {
        Optional<Delivery> result = deliveryService.getDeliveryById(deliveryId);
        if (result.isPresent()) {
            deliveryService.deleteDelivery(deliveryId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
