package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.DeliveryStatus;

public record DeliveryResponse(
        Integer id,
        Integer orderId,
        String shippingMethod,
        String trackingNumber,
        DeliveryStatus status,
        Date dispatchedAt) {

    public static DeliveryResponse from(Delivery delivery) {
        var order = delivery.getOrder();
        return new DeliveryResponse(
                delivery.getId(),
                order != null ? order.getId() : null,
                delivery.getShippingMethod(),
                delivery.getTrackingNumber(),
                delivery.getStatus(),
                delivery.getDispatchedAt());
    }
}
