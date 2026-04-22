package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class TagRequest {
    private Integer productId;
    private Integer categoryId;
    private String name;
}
