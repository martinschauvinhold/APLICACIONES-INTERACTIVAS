package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.dto.PaymentRequest;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    public ArrayList<Payment> getPayments() {
        return new ArrayList<>(paymentRepository.findAll());
    }

    public Optional<Payment> getPaymentById(int paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public List<Payment> getPaymentsByOrder(int orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Payment createPayment(PaymentRequest paymentRequest) {
        Order order = orderRepository.findById(paymentRequest.getOrderId()).get();
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .transactionId(paymentRequest.getTransactionId())
                .paymentStatus(paymentRequest.getPaymentStatus())
                .paidAt(new Date())
                .build();
        return paymentRepository.save(payment);
    }

    public Payment updatePayment(int paymentId, PaymentRequest paymentRequest) {
        Payment payment = paymentRepository.findById(paymentId).get();
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setTransactionId(paymentRequest.getTransactionId());
        payment.setPaymentStatus(paymentRequest.getPaymentStatus());
        return paymentRepository.save(payment);
    }

    public void deletePayment(int paymentId) {
        paymentRepository.deleteById(paymentId);
    }
}
