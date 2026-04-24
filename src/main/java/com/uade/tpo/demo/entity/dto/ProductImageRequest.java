package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class ProductImageRequest {
    private int productId;
    private String url;
    private boolean isPrimary;
    private int sortOrder;
}