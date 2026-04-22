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

import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.service.OrderItemService;

@RestController
@RequestMapping("order-items")
public class OrderItemsController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<List<OrderItem>> getItemsByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(orderItemService.getItemsByOrder(orderId));
    }

    @GetMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<OrderItem> getItemById(@PathVariable int itemId) {
        Optional<OrderItem> result = orderItemService.getItemById(itemId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Object> deleteItem(@PathVariable int itemId) {
        Optional<OrderItem> result = orderItemService.getItemById(itemId);
        if (result.isPresent()) {
            orderItemService.deleteItem(itemId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
