package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class DiscountRequest {
    private String name;
    private String discountType;
    private BigDecimal value;
    private String appliesTo;
    private Integer productId;
    private Integer categoryId;
    private BigDecimal minPrice;
    private Date startsAt;
    private Date expiresAt;
}
