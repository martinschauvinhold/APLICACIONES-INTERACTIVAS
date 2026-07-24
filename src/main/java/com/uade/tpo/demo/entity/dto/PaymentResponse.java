package com.uade.tpo.demo.entity.dto;

import java.util.Date;

import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.PaymentStatus;

public record PaymentResponse(
        Integer id,
        Integer orderId,
        String paymentMethod,
        String transactionId,
        PaymentStatus paymentStatus,
        Date paidAt) {

    public static PaymentResponse from(Payment payment) {
        var order = payment.getOrder();
        return new PaymentResponse(
                payment.getId(),
                order != null ? order.getId() : null,
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getPaymentStatus(),
                payment.getPaidAt());
    }
}
