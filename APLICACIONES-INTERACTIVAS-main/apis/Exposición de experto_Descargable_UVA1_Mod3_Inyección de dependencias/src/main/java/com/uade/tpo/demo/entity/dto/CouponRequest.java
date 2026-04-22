package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CouponRequest {
    @Positive
    private int discountId;
    @NotBlank
    private String code;
    private Integer usageLimit;
    private Date expiresAt;
}
