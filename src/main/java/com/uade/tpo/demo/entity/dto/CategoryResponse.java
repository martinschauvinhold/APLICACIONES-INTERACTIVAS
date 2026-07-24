package com.uade.tpo.demo.entity.dto;

import com.uade.tpo.demo.entity.Category;

public record CategoryResponse(
        Integer id,
        String description,
        String slug,
        boolean isActive) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getDescription(),
                category.getSlug(),
                category.isActive());
    }
}
