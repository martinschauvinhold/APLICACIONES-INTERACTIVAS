package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.RefundStatus;

import jakarta.validation.constraints.NotNull;

public record RefundStatusRequest(@NotNull RefundStatus status) {}
