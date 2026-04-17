package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.dto.PaymentRequest;
import com.uade.tpo.demo.service.PaymentService;

@RestController
@RequestMapping("payments")
public class PaymentsController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ArrayList<Payment>> getPayments() {
        return ResponseEntity.ok(paymentService.getPayments());
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable int paymentId) {
        Optional<Payment> result = paymentService.getPaymentById(paymentId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<Object> createPayment(
            @RequestBody PaymentRequest paymentRequest,
            @RequestParam(required = false, defaultValue = "false") boolean simulateFailure) {
        Payment result = paymentService.createPayment(paymentRequest, simulateFailure);
        return ResponseEntity.created(URI.create("/payments/" + result.getId())).body(result);
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<Object> updatePayment(@PathVariable int paymentId, @RequestBody PaymentRequest paymentRequest) {
        Optional<Payment> result = paymentService.getPaymentById(paymentId);
        if (result.isPresent()) {
            Payment updated = paymentService.updatePayment(paymentId, paymentRequest);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Object> deletePayment(@PathVariable int paymentId) {
        Optional<Payment> result = paymentService.getPaymentById(paymentId);
        if (result.isPresent()) {
            paymentService.deletePayment(paymentId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
