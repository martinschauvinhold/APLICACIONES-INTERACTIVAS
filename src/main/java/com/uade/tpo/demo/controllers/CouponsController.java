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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.dto.CouponRequest;
import com.uade.tpo.demo.entity.dto.CouponResponse;
import com.uade.tpo.demo.service.CouponService;

@RestController
@RequestMapping("coupons")
public class CouponsController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getCoupons() {
        List<CouponResponse> result = couponService.getCoupons().stream().map(CouponResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable int couponId) {
        Optional<Coupon> result = couponService.getCouponById(couponId);
        return result.map(c -> ResponseEntity.ok(CouponResponse.from(c))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity<Object> createCoupon(@Valid @RequestBody CouponRequest request) {
        Coupon result = couponService.createCoupon(request);
        return ResponseEntity.created(URI.create("/coupons/" + result.getId())).body(CouponResponse.from(result));
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Object> deleteCoupon(@PathVariable int couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('buyer','admin')")
    @GetMapping("/validate/{code}")
    public ResponseEntity<CouponResponse> validateCoupon(@PathVariable String code) {
        Coupon coupon = couponService.validateCoupon(code);
        return ResponseEntity.ok(CouponResponse.from(coupon));
    }
}
