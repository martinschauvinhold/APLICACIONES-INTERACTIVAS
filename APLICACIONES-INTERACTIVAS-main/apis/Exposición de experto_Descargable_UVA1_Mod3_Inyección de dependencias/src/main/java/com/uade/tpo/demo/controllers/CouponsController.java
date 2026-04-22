package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponService.getCoupons());
    }

    @GetMapping("/{couponId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Coupon> getCouponById(@PathVariable int couponId) {
        Optional<Coupon> result = couponService.getCouponById(couponId);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Object> createCoupon(@RequestBody CouponRequest request) {
        Coupon result = couponService.createCoupon(request);
        return ResponseEntity.created(URI.create("/coupons/" + result.getId())).body(result);
    }

    @DeleteMapping("/{couponId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteCoupon(@PathVariable int couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/{code}")
    @PreAuthorize("hasAnyRole('buyer', 'seller', 'admin')")
    public ResponseEntity<Coupon> validateCoupon(@PathVariable String code) {
        Coupon coupon = couponService.validateCoupon(code);
        return ResponseEntity.ok(coupon);
    }
}
