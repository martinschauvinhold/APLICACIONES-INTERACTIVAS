package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PriceTierRequest {
    private int variantId;
    private int minQuantity;
    private BigDecimal unitPrice;
}