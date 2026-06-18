package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PriceTierRequest {
    @Positive private int minQuantity;
    @NotNull @Positive private BigDecimal unitPrice;
}
