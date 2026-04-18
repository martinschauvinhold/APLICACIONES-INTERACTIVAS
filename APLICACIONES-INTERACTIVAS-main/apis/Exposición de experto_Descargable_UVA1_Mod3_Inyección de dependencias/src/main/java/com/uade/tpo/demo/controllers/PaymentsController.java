package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    /**
     * Procesa el pago de una orden.
     * - Valida que la orden este en PENDING
     * - Bloquea el inventario y revalida stock
     * - Si el pago es exitoso: descuenta stock y marca orden como PAID
     * - Si el pago falla: la orden queda en PENDING
     *
     * Query param opcional: ?simulateFailure=true para forzar fallo (solo testing).
     */
    @PostMapping
    public ResponseEntity<Object> processPayment(
            @RequestBody PaymentRequest paymentRequest,
            @RequestParam(required = false, defaultValue = "false") boolean simulateFailure) {
        Payment result = paymentService.processPayment(paymentRequest, simulateFailure);
        return ResponseEntity.created(URI.create("/payments/" + result.getId())).body(result);
    }
}
