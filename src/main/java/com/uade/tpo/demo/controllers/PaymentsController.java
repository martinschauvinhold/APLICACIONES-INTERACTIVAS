package com.uade.tpo.demo.controllers;

import java.net.URI;
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
import com.uade.tpo.demo.entity.dto.PaymentResponse;
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
    public ResponseEntity<List<PaymentResponse>> getPayments() {
        List<PaymentResponse> result = paymentService.getPayments().stream().map(PaymentResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable int paymentId) {
        Optional<Payment> result = paymentService.getPaymentById(paymentId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorizationService.requireSelfOrAdmin(result.get().getOrder().getUser().getId());
        return ResponseEntity.ok(PaymentResponse.from(result.get()));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable int orderId) {
        authorizationService.requireSelfOrAdmin(orderOwnerId(orderId));
        List<PaymentResponse> result = paymentService.getPaymentsByOrder(orderId).stream().map(PaymentResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest,
                                                   @RequestParam(defaultValue = "false") boolean simulateFailure) {
        authorizationService.requireSelfOrAdmin(orderOwnerId(paymentRequest.getOrderId()));
        Payment result = paymentService.processPayment(paymentRequest, simulateFailure);
        return ResponseEntity.created(URI.create("/payments/" + result.getId())).body(PaymentResponse.from(result));
    }

    private int orderOwnerId(int orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        return order.getUser().getId();
    }
}
