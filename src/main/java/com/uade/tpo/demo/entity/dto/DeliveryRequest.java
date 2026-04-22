package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.DeliveryStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryRequest(
        @NotNull Integer orderId,
        @NotBlank String shippingMethod,
        @NotBlank String trackingNumber,
        DeliveryStatus status
) {}
