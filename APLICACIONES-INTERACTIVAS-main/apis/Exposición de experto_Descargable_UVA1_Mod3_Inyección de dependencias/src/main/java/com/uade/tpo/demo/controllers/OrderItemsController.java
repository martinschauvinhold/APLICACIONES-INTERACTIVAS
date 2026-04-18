package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.dto.OrderItemRequest;
import com.uade.tpo.demo.service.OrderItemService;

@RestController
@RequestMapping("order-items")
public class OrderItemsController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getItemsByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(orderItemService.getItemsByOrder(orderId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<OrderItem> getItemById(@PathVariable int itemId) {
        Optional<OrderItem> result = orderItemService.getItemById(itemId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<Object> addItem(@PathVariable int orderId, @RequestBody OrderItemRequest request) {
        OrderItem result = orderItemService.addItem(orderId, request);
        return ResponseEntity.created(URI.create("/order-items/" + result.getId())).body(result);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable int itemId) {
        Optional<OrderItem> result = orderItemService.getItemById(itemId);
        if (result.isPresent()) {
            orderItemService.deleteItem(itemId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
