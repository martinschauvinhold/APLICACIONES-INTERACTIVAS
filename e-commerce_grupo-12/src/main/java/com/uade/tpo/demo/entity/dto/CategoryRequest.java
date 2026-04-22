package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    private int id;
    @NotBlank private String description;
}
