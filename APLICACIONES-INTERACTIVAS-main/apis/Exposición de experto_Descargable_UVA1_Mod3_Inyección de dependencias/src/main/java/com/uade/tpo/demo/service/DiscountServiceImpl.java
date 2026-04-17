package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.DiscountRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public ArrayList<Discount> getDiscounts() {
        return new ArrayList<>(discountRepository.findAll());
    }

    public Optional<Discount> getDiscountById(int discountId) {
        return discountRepository.findById(discountId);
    }

    public Discount createDiscount(DiscountRequest request) {
        Product product = null;
        Category category = null;

        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));
        }
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
        }

        Discount discount = Discount.builder()
                .name(request.getName())
                .discountType(request.getDiscountType())
                .value(request.getValue())
                .appliesTo(request.getAppliesTo())
                .product(product)
                .category(category)
                .minPrice(request.getMinPrice())
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .isActive(true)
                .build();

        return discountRepository.save(discount);
    }

    public Discount updateDiscount(int discountId, DiscountRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new NotFoundException("Discount", discountId));

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));
            discount.setProduct(product);
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            discount.setCategory(category);
        }

        discount.setName(request.getName());
        discount.setDiscountType(request.getDiscountType());
        discount.setValue(request.getValue());
        discount.setAppliesTo(request.getAppliesTo());
        discount.setMinPrice(request.getMinPrice());
        discount.setStartsAt(request.getStartsAt());
        discount.setExpiresAt(request.getExpiresAt());

        return discountRepository.save(discount);
    }

    public void deleteDiscount(int discountId) {
        discountRepository.deleteById(discountId);
    }

    public List<Discount> getActiveDiscountsForProduct(int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        List<Discount> productDiscounts = discountRepository.findByProductIdAndIsActiveTrue(productId);
        List<Discount> categoryDiscounts = discountRepository
                .findByCategoryIdAndIsActiveTrue(product.getCategory().getId());

        Date now = new Date();

        return Stream.concat(productDiscounts.stream(), categoryDiscounts.stream())
                .filter(d -> d.getStartsAt() == null || !d.getStartsAt().after(now))
                .filter(d -> d.getExpiresAt() == null || !d.getExpiresAt().before(now))
                .collect(Collectors.toList());
    }
}
