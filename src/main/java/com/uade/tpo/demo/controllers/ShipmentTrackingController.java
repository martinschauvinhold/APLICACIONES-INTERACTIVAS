package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.ShipmentTracking;
import com.uade.tpo.demo.entity.dto.ShipmentTrackingRequest;
import com.uade.tpo.demo.entity.dto.ShipmentTrackingResponse;
import com.uade.tpo.demo.entity.dto.TrackingStatusRequest;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.DeliveryService;
import com.uade.tpo.demo.service.ShipmentTrackingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("tracking")
@RequiredArgsConstructor
public class ShipmentTrackingController {

    private final ShipmentTrackingService trackingService;
    private final DeliveryService deliveryService;
    private final AuthorizationService authorizationService;

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<ShipmentTrackingResponse>> getByDelivery(@PathVariable Integer deliveryId) {
        Delivery delivery = deliveryService.getDeliveryById(deliveryId);
        authorizationService.requireSelfOrAdmin(delivery.getOrder().getUser().getId());
        List<ShipmentTrackingResponse> result = trackingService.getByDeliveryId(deliveryId).stream()
                .map(ShipmentTrackingResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<ShipmentTrackingResponse> getById(@PathVariable Integer trackingId) {
        ShipmentTracking tracking = trackingService.getById(trackingId);
        authorizationService.requireSelfOrAdmin(tracking.getDelivery().getOrder().getUser().getId());
        return ResponseEntity.ok(ShipmentTrackingResponse.from(tracking));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<ShipmentTrackingResponse> addCheckpoint(@Valid @RequestBody ShipmentTrackingRequest request) {
        ShipmentTracking created = trackingService.addCheckpoint(request);
        return ResponseEntity.created(URI.create("/tracking/" + created.getId())).body(ShipmentTrackingResponse.from(created));
    }

    @PutMapping("/{trackingId}/status")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<ShipmentTrackingResponse> updateStatus(@PathVariable Integer trackingId,
                                                          @Valid @RequestBody TrackingStatusRequest request) {
        return ResponseEntity.ok(ShipmentTrackingResponse.from(trackingService.updateStatus(trackingId, request.status())));
    }
}
