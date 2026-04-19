package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.dto.DiscountRequest;

public interface DiscountService {

    List<Discount> getDiscounts();

    Optional<Discount> getDiscountById(int discountId);

    Discount createDiscount(DiscountRequest request);

    Discount updateDiscount(int discountId, DiscountRequest request);

    void deleteDiscount(int discountId);

    List<Discount> getActiveDiscountsForProduct(int productId);
}
