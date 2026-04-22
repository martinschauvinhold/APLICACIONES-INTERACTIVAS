package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.ReturnStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductReturnRequest(
        @NotNull Integer orderId,
        @NotBlank String reason,
        ReturnStatus status
) {}
