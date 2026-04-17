package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Optional;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.dto.CouponRequest;

public interface CouponService {
    public ArrayList<Coupon> getCoupons();

    public Optional<Coupon> getCouponById(int couponId);

    public Coupon createCoupon(CouponRequest couponRequest);

    public void deleteCoupon(int couponId);

    public Coupon validateCoupon(String code);
}
