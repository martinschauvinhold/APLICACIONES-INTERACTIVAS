package com.uade.tpo.demo.entity.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderRequest {
    private int userId;
    private int shippingAddressId;
    private String couponCode;
    @Valid @NotEmpty private List<OrderItemRequest> items;
}
