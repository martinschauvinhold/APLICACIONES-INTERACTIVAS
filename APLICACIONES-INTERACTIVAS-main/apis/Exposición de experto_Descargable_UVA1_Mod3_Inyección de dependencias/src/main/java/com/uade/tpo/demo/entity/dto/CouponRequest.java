package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import lombok.Data;

@Data
public class CouponRequest {
    private int discountId;
    private String code;
    private Integer usageLimit;
    private Date expiresAt;
}
