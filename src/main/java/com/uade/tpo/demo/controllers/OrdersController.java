package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.dto.OrderRequest;
import com.uade.tpo.demo.entity.dto.OrderResponse;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.OrderService;

@RestController
@RequestMapping("orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        List<OrderResponse> result = orderService.getOrders().stream().map(OrderResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable int orderId) {
        Optional<Order> result = orderService.getOrderById(orderId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorizationService.requireSelfOrAdmin(result.get().getUser().getId());
        return ResponseEntity.ok(OrderResponse.from(result.get()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable int userId) {
        authorizationService.requireSelfOrAdmin(userId);
        List<OrderResponse> result = orderService.getOrdersByUser(userId).stream().map(OrderResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<Object> createOrder(@RequestBody OrderRequest orderRequest) {
        // El comprador solo puede crear órdenes para sí mismo: se ignora
        // cualquier userId que venga en el body y se usa el del JWT.
        orderRequest.setUserId(authorizationService.currentUser().getId());
        Order result = orderService.createOrder(orderRequest);
        return ResponseEntity.created(URI.create("/orders/" + result.getId())).body(OrderResponse.from(result));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Object> updateOrder(@PathVariable int orderId, @RequestBody OrderRequest orderRequest) {
        Optional<Order> result = orderService.getOrderById(orderId);
        if (result.isPresent()) {
            authorizationService.requireSelfOrAdmin(result.get().getUser().getId());
            Order updated = orderService.updateOrder(orderId, orderRequest);
            return ResponseEntity.ok(OrderResponse.from(updated));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteOrder(@PathVariable int orderId) {
        Optional<Order> result = orderService.getOrderById(orderId);
        if (result.isPresent()) {
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable int orderId) {
        Optional<Order> existing = orderService.getOrderById(orderId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorizationService.requireSelfOrAdmin(existing.get().getUser().getId());
        Order result = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(OrderResponse.from(result));
    }

    @DeleteMapping("/expired")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Integer> cancelExpiredOrders() {
        int count = orderService.cancelExpiredOrders();
        return ResponseEntity.ok(count);
    }
}
