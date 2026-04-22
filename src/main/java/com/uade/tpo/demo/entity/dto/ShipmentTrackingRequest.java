package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.TrackingStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShipmentTrackingRequest(
        @NotNull Integer deliveryId,
        @NotBlank String checkpoint,
        @NotNull TrackingStatus status
) {}
