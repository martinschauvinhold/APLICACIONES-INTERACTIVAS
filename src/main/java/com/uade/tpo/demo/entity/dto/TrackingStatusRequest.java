package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.TrackingStatus;

import jakarta.validation.constraints.NotNull;

public record TrackingStatusRequest(@NotNull TrackingStatus status) {}
