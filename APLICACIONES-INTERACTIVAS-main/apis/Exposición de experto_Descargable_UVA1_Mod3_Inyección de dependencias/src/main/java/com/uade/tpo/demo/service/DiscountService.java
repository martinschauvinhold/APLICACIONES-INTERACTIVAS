package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.dto.DiscountRequest;

public interface DiscountService {
    public ArrayList<Discount> getDiscounts();

    public Optional<Discount> getDiscountById(int discountId);

    public Discount createDiscount(DiscountRequest discountRequest);

    public Discount updateDiscount(int discountId, DiscountRequest discountRequest);

    public void deleteDiscount(int discountId);

    public List<Discount> getActiveDiscountsForProduct(int productId);
}
