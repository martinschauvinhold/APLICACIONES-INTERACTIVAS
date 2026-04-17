package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.dto.CouponRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CouponRepository;
import com.uade.tpo.demo.repository.DiscountRepository;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DiscountRepository discountRepository;

    public ArrayList<Coupon> getCoupons() {
        return new ArrayList<>(couponRepository.findAll());
    }

    public Optional<Coupon> getCouponById(int couponId) {
        return couponRepository.findById(couponId);
    }

    public Coupon createCoupon(CouponRequest request) {
        Discount discount = discountRepository.findById(request.getDiscountId())
                .orElseThrow(() -> new NotFoundException("Discount", request.getDiscountId()));

        Coupon coupon = Coupon.builder()
                .discount(discount)
                .code(request.getCode())
                .usageLimit(request.getUsageLimit())
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .timesUsed(0)
                .build();

        return couponRepository.save(coupon);
    }

    public void deleteCoupon(int couponId) {
        couponRepository.deleteById(couponId);
    }

    public Coupon validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Coupon", code));

        if (!coupon.isActive()) {
            throw new BusinessRuleException("El cupón no está activo");
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().before(new Date())) {
            throw new BusinessRuleException("El cupón ha expirado");
        }

        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new BusinessRuleException("El cupón ha alcanzado su límite de uso");
        }

        return coupon;
    }
}
