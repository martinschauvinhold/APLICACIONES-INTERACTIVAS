package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;

public record PriceTierResponse(int minQuantity, BigDecimal unitPrice, String currency) {
}
