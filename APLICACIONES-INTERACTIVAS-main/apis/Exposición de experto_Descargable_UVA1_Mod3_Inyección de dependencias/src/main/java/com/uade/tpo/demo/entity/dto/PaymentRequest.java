package com.uade.tpo.demo.entity.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private int orderId;
    private String paymentMethod;
    private boolean simulateFailure;
}
