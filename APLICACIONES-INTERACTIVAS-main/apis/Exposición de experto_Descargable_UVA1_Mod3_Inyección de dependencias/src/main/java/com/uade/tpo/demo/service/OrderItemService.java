package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.dto.OrderItemRequest;

public interface OrderItemService {
    List<OrderItem> getItemsByOrder(int orderId);

    Optional<OrderItem> getItemById(int itemId);

    OrderItem addItem(int orderId, OrderItemRequest request);

    void deleteItem(int itemId);
}
