package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private String brand;
    private int categoryId;
}
