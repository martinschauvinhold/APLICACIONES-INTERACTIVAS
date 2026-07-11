package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {
}
