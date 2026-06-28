package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.ShipmentTracking;
import com.uade.tpo.demo.entity.TrackingStatus;

public record ShipmentTrackingResponse(
        Integer id,
        Integer deliveryId,
        String checkpoint,
        TrackingStatus status,
        Date recordedAt) {

    public static ShipmentTrackingResponse from(ShipmentTracking tracking) {
        var delivery = tracking.getDelivery();
        return new ShipmentTrackingResponse(
                tracking.getId(),
                delivery != null ? delivery.getId() : null,
                tracking.getCheckpoint(),
                tracking.getStatus(),
                tracking.getRecordedAt());
    }
}
