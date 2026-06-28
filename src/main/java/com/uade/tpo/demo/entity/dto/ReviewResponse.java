package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Review;

public record ReviewResponse(
        Integer id,
        Integer userId,
        String username,
        Integer productId,
        String productName,
        int rating,
        String comment,
        Date createdAt) {

    public static ReviewResponse from(Review review) {
        var user = review.getUser();
        var product = review.getProduct();
        return new ReviewResponse(
                review.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}
