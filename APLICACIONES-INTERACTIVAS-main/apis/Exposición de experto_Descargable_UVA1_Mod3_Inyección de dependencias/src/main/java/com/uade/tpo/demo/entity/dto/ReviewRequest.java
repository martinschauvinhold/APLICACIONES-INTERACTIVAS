package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private int userId;
    private int productId;
    private int rating;
    private String comment;
}
