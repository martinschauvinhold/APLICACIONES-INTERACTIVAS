package com.uade.tpo.demo.entity.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequest {
    private int userId;
    private int shippingAddressId;
    private String couponCode;
    private List<OrderItemRequest> items;
}
