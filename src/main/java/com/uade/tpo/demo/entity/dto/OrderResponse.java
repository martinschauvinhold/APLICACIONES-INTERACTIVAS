package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderStatus;

public record OrderResponse(
        Integer id,
        Integer userId,
        String username,
        Integer shippingAddressId,
        AddressResponse shippingAddress,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        Date createdAt,
        Date updatedAt) {

    public static OrderResponse from(Order order) {
        var user = order.getUser();
        var address = order.getShippingAddress();
        return new OrderResponse(
                order.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                address != null ? address.getId() : null,
                address != null ? AddressResponse.from(address) : null,
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
