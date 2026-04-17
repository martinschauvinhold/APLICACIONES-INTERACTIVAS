package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.dto.CouponRequest;
import com.uade.tpo.demo.service.CouponService;

@RestController
@RequestMapping("coupons")
public class CouponsController {

    @Autowired
    private CouponService couponService;

    @PostMapping
    public ResponseEntity<Object> createCoupon(@RequestBody CouponRequest couponRequest) {
        Coupon result = couponService.createCoupon(couponRequest);
        return ResponseEntity.created(URI.create("/coupons/" + result.getId())).body(result);
    }

    @GetMapping
    public ResponseEntity<ArrayList<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponService.getCoupons());
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable int couponId) {
        Optional<Coupon> result = couponService.getCouponById(couponId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Object> deleteCoupon(@PathVariable int couponId) {
        Optional<Coupon> result = couponService.getCouponById(couponId);
        if (result.isPresent()) {
            couponService.deleteCoupon(couponId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<Coupon> validateCoupon(@PathVariable String code) {
        Coupon coupon = couponService.validateCoupon(code);
        return ResponseEntity.ok(coupon);
    }
}
