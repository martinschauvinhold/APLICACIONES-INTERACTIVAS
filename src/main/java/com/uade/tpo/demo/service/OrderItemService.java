package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.OrderItem;

public interface OrderItemService {
    List<OrderItem> getItemsByOrder(int orderId);
    Optional<OrderItem> getItemById(int itemId);
    void deleteItem(int itemId);
    List<OrderItem> getItemsBySeller(int sellerId);
}
