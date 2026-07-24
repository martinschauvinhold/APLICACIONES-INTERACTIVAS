package com.uade.tpo.demo.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.uade.tpo.demo.entity.Refund;
import com.uade.tpo.demo.entity.RefundStatus;

public record RefundResponse(
        Integer id,
        Integer returnId,
        BigDecimal amount,
        String currency,
        RefundStatus status,
        Date processedAt) {

    public static RefundResponse from(Refund refund) {
        var productReturn = refund.getProductReturn();
        return new RefundResponse(
                refund.getId(),
                productReturn != null ? productReturn.getId() : null,
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getProcessedAt());
    }
}
