package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Coupon;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.dto.CouponRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.DuplicateException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CouponRepository;
import com.uade.tpo.demo.repository.DiscountRepository;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Override
    public List<Coupon> getCoupons() {
        return new ArrayList<>(couponRepository.findAll());
    }

    @Override
    public Optional<Coupon> getCouponById(int couponId) {
        return couponRepository.findById(couponId);
    }

    @Override
    public Coupon createCoupon(CouponRequest request) {
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateException("Coupon", "code", request.getCode());
        }

        Discount discount = discountRepository.findById(request.getDiscountId())
                .orElseThrow(() -> new NotFoundException("Discount", request.getDiscountId()));

        Coupon coupon = Coupon.builder()
                .discount(discount)
                .code(request.getCode())
                .usageLimit(request.getUsageLimit())
                .timesUsed(0)
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .build();

        return couponRepository.save(coupon);
    }

    @Override
    public void deleteCoupon(int couponId) {
        if (!couponRepository.existsById(couponId)) {
            throw new NotFoundException("Coupon", couponId);
        }
        couponRepository.deleteById(couponId);
    }

    @Override
    public Coupon validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Coupon", code));

        if (!coupon.isActive()) {
            throw new BusinessRuleException("El cupon '" + code + "' esta desactivado");
        }

        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().before(new Date())) {
            throw new BusinessRuleException("El cupon '" + code + "' esta expirado");
        }

        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new BusinessRuleException("El cupon '" + code + "' alcanzo el limite de usos ("
                    + coupon.getUsageLimit() + ")");
        }

        return coupon;
    }
}
