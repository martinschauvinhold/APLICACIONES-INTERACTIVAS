package com.uade.tpo.demo.service;

import java.math.BigDecimal;

import com.uade.tpo.demo.entity.dto.PaymentResult;

public interface PaymentProcessor {

    PaymentResult process(BigDecimal amount, String paymentMethod);
}
