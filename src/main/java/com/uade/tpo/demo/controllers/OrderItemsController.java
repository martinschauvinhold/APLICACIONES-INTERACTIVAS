package com.uade.tpo.demo.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.dto.OrderItemResponse;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.OrderItemService;
import com.uade.tpo.demo.service.OrderService;

@RestController
@RequestMapping("order-items")
public class OrderItemsController {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<OrderItemResponse>> getItemsByOrder(@PathVariable int orderId) {
        authorizationService.requireSelfOrAdmin(orderOwnerId(orderId));
        List<OrderItemResponse> result = orderItemService.getItemsByOrder(orderId).stream()
                .map(OrderItemResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seller/{sellerId}")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<List<OrderItemResponse>> getItemsBySeller(@PathVariable int sellerId) {
        authorizationService.requireSelfOrAdmin(sellerId);
        List<OrderItemResponse> result = orderItemService.getItemsBySeller(sellerId).stream()
                .map(OrderItemResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<OrderItemResponse> getItemById(@PathVariable int itemId) {
        Optional<OrderItem> result = orderItemService.getItemById(itemId);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        authorizationService.requireSelfOrAdmin(result.get().getOrder().getUser().getId());
        return ResponseEntity.ok(OrderItemResponse.from(result.get()));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Object> deleteItem(@PathVariable int itemId) {
        Optional<OrderItem> result = orderItemService.getItemById(itemId);
        if (result.isPresent()) {
            authorizationService.requireSelfOrAdmin(result.get().getOrder().getUser().getId());
            orderItemService.deleteItem(itemId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private int orderOwnerId(int orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        return order.getUser().getId();
    }
}
