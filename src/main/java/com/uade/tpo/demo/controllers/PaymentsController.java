package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.Payment;
import com.uade.tpo.demo.entity.dto.PaymentRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.OrderService;
import com.uade.tpo.demo.service.PaymentService;

@RestController
@RequestMapping("payments")
public class PaymentsController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ArrayList<Payment>> getPayments() {
        return ResponseEntity.ok(paymentService.getPayments());
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Payment> getPaymentById(@PathVariable int paymentId) {
        Optional<Payment> result = paymentService.getPaymentById(paymentId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorizationService.requireSelfOrAdmin(result.get().getOrder().getUser().getId());
        return ResponseEntity.ok(result.get());
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable int orderId) {
        authorizationService.requireSelfOrAdmin(orderOwnerId(orderId));
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<Payment> processPayment(@RequestBody PaymentRequest paymentRequest,
                                                   @RequestParam(defaultValue = "false") boolean simulateFailure) {
        authorizationService.requireSelfOrAdmin(orderOwnerId(paymentRequest.getOrderId()));
        Payment result = paymentService.processPayment(paymentRequest, simulateFailure);
        return ResponseEntity.created(URI.create("/payments/" + result.getId())).body(result);
    }

    private int orderOwnerId(int orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        return order.getUser().getId();
    }
}
