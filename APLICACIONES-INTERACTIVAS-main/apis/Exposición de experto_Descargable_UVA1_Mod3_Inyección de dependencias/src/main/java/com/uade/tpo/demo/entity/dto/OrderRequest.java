package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private int userId;
    private int shippingAddressId;
    private String status;
}
