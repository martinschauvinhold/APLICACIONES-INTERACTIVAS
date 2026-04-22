package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.ShipmentTracking;
import com.uade.tpo.demo.entity.TrackingStatus;
import com.uade.tpo.demo.entity.dto.ShipmentTrackingRequest;

public interface ShipmentTrackingService {
    List<ShipmentTracking> getByDeliveryId(Integer deliveryId);

    ShipmentTracking getById(Integer trackingId);

    ShipmentTracking addCheckpoint(ShipmentTrackingRequest request);

    ShipmentTracking updateStatus(Integer trackingId, TrackingStatus status);
}
