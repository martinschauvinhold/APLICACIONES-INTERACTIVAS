package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.dto.CouponRequest;

public interface CouponService {

    List<Coupon> getCoupons();

    Optional<Coupon> getCouponById(int couponId);

    Coupon createCoupon(CouponRequest request);

    void deleteCoupon(int couponId);

    Coupon validateCoupon(String code);
}
