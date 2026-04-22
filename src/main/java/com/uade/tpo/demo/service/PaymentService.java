package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.dto.PaymentRequest;

public interface PaymentService {
    public ArrayList<Payment> getPayments();

    public Optional<Payment> getPaymentById(int paymentId);

    public List<Payment> getPaymentsByOrder(int orderId);

    public Payment processPayment(PaymentRequest paymentRequest, boolean simulateFailure);
}
