package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class DeliveryRequest {
    private int orderId;
    private String shippingMethod;
    private String trackingNumber;
    private String deliveryStatus;
}
