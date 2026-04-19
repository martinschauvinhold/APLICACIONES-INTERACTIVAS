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

    @Override
    public List<Discount> getDiscounts() {
        return new ArrayList<>(discountRepository.findAll());
    }

    @Override
    public Optional<Discount> getDiscountById(int discountId) {
        return discountRepository.findById(discountId);
    }

    @Override
    public Discount createDiscount(DiscountRequest request) {
        Discount.DiscountBuilder builder = Discount.builder()
                .name(request.getName())
                .discountType(request.getDiscountType())
                .value(request.getValue())
                .appliesTo(request.getAppliesTo())
                .minPrice(request.getMinPrice())
                .startsAt(request.getStartsAt())
                .expiresAt(request.getExpiresAt())
                .isActive(true);

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));
            builder.product(product);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            builder.category(category);
        }

        return discountRepository.save(builder.build());
    }

    @Override
    public Discount updateDiscount(int discountId, DiscountRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new NotFoundException("Discount", discountId));

        discount.setName(request.getName());
        discount.setDiscountType(request.getDiscountType());
        discount.setValue(request.getValue());
        discount.setAppliesTo(request.getAppliesTo());
        discount.setMinPrice(request.getMinPrice());
        discount.setStartsAt(request.getStartsAt());
        discount.setExpiresAt(request.getExpiresAt());

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));
            discount.setProduct(product);
        } else {
            discount.setProduct(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.getCategoryId()));
            discount.setCategory(category);
        } else {
            discount.setCategory(null);
        }

        return discountRepository.save(discount);
    }

    @Override
    public void deleteDiscount(int discountId) {
        if (!discountRepository.existsById(discountId)) {
            throw new NotFoundException("Discount", discountId);
        }
        discountRepository.deleteById(discountId);
    }

    @Override
    public List<Discount> getActiveDiscountsForProduct(int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));

        Date now = new Date();

        // Descuentos por producto directo
        List<Discount> byProduct = discountRepository.findByProductIdAndIsActiveTrue(productId);

        // Descuentos por categoria del producto
        List<Discount> byCategory = discountRepository
                .findByCategoryIdAndIsActiveTrue(product.getCategory().getId());

        // Unir y filtrar por vigencia (startsAt <= now <= expiresAt)
        return Stream.concat(byProduct.stream(), byCategory.stream())
                .filter(d -> d.getStartsAt() == null || !d.getStartsAt().after(now))
                .filter(d -> d.getExpiresAt() == null || !d.getExpiresAt().before(now))
                .collect(Collectors.toList());
    }
}
