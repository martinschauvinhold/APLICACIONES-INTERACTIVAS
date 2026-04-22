package com.uade.tpo.demo.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReviewRequest {

    @Positive
    private int userId;

    @Positive
    private int productId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;
}
