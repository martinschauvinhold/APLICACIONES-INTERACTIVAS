package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Coupon;

public record CouponResponse(
        Integer id,
        Integer discountId,
        String discountName,
        String discountType,
        String code,
        Integer usageLimit,
        int timesUsed,
        Date expiresAt,
        boolean isActive) {

    public static CouponResponse from(Coupon coupon) {
        var discount = coupon.getDiscount();
        return new CouponResponse(
                coupon.getId(),
                discount != null ? discount.getId() : null,
                discount != null ? discount.getName() : null,
                discount != null ? discount.getDiscountType() : null,
                coupon.getCode(),
                coupon.getUsageLimit(),
                coupon.getTimesUsed(),
                coupon.getExpiresAt(),
                coupon.isActive());
    }
}
