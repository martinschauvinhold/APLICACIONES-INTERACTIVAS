package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RefundRequest(
        @NotNull Integer returnId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String currency
) {}
