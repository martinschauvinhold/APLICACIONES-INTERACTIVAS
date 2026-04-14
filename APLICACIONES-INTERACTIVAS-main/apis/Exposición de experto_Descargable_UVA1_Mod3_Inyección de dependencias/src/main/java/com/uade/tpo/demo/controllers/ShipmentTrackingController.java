package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.ShipmentTracking;
import com.uade.tpo.demo.entity.dto.ShipmentTrackingRequest;
import com.uade.tpo.demo.entity.dto.TrackingStatusRequest;
import com.uade.tpo.demo.service.ShipmentTrackingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("tracking")
@RequiredArgsConstructor
public class ShipmentTrackingController {

    private final ShipmentTrackingService trackingService;

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<ShipmentTracking>> getByDelivery(@PathVariable Integer deliveryId) {
        return ResponseEntity.ok(trackingService.getByDeliveryId(deliveryId));
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<ShipmentTracking> getById(@PathVariable Integer trackingId) {
        return ResponseEntity.ok(trackingService.getById(trackingId));
    }

    @PostMapping
    public ResponseEntity<ShipmentTracking> addCheckpoint(@Valid @RequestBody ShipmentTrackingRequest request) {
        ShipmentTracking created = trackingService.addCheckpoint(request);
        return ResponseEntity.created(URI.create("/tracking/" + created.getId())).body(created);
    }

    @PutMapping("/{trackingId}/status")
    public ResponseEntity<ShipmentTracking> updateStatus(@PathVariable Integer trackingId,
                                                          @Valid @RequestBody TrackingStatusRequest request) {
        return ResponseEntity.ok(trackingService.updateStatus(trackingId, request.status()));
    }
}
