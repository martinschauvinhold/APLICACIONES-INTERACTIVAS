package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.dto.PaymentRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentProcessor paymentProcessor;

    public ArrayList<Payment> getPayments() {
        return new ArrayList<>(paymentRepository.findAll());
    }

    public Optional<Payment> getPaymentById(int paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public List<Payment> getPaymentsByOrder(int orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // TODO: sera reescrito en PR 4 con logica de pago real, locking y descuento de stock
    public Payment createPayment(PaymentRequest paymentRequest, boolean simulateFailure) {
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order", paymentRequest.getOrderId()));

        // Configurar simulacion de fallo si se pide (flag de testing, no en prod)
        if (paymentProcessor instanceof SimulatedPaymentProcessor simulated) {
            simulated.setSimulateFailure(simulateFailure);
        }

        var result = paymentProcessor.process(order.getTotalAmount(), paymentRequest.getPaymentMethod());

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .transactionId(result.getTransactionId())
                .paymentStatus(result.getStatus())
                .paidAt(new java.util.Date())
                .build();

        return paymentRepository.save(payment);
    }

    public Payment updatePayment(int paymentId, PaymentRequest paymentRequest) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment", paymentId));
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        return paymentRepository.save(payment);
    }

    public void deletePayment(int paymentId) {
        paymentRepository.deleteById(paymentId);
    }
}
