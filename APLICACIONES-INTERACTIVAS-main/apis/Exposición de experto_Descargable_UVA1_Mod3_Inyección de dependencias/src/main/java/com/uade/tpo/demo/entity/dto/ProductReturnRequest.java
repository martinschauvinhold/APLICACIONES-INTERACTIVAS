package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class ProductReturnRequest {
    private int orderId;
    private String reason;
    private String status;
}
