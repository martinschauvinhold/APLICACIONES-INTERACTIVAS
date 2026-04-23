package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
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
import com.uade.tpo.demo.service.OrderService;

@RestController
@RequestMapping("orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<ArrayList<Order>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Order> getOrderById(@PathVariable int orderId) {
        Optional<Order> result = orderService.getOrderById(orderId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable int userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<Object> createOrder(@RequestBody OrderRequest orderRequest) {
        Order result = orderService.createOrder(orderRequest);
        return ResponseEntity.created(URI.create("/orders/" + result.getId())).body(result);
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Object> updateOrder(@PathVariable int orderId, @RequestBody OrderRequest orderRequest) {
        Optional<Order> result = orderService.getOrderById(orderId);
        if (result.isPresent()) {
            Order updated = orderService.updateOrder(orderId, orderRequest);
            return ResponseEntity.ok(updated);
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
    public ResponseEntity<Order> cancelOrder(@PathVariable int orderId) {
        Order result = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/expired")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Integer> cancelExpiredOrders() {
        int count = orderService.cancelExpiredOrders();
        return ResponseEntity.ok(count);
    }
}
