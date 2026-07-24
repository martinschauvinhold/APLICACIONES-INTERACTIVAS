package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.ReturnStatus;

public record ProductReturnResponse(
        Integer id,
        Integer orderId,
        String reason,
        ReturnStatus status,
        Date requestedAt) {

    public static ProductReturnResponse from(ProductReturn productReturn) {
        var order = productReturn.getOrder();
        return new ProductReturnResponse(
                productReturn.getId(),
                order != null ? order.getId() : null,
                productReturn.getReason(),
                productReturn.getStatus(),
                productReturn.getRequestedAt());
    }
}
