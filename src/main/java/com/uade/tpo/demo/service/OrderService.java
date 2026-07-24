package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.entity.dto.OrderRequest;

public interface OrderService {
    public ArrayList<Order> getOrders();

    public Optional<Order> getOrderById(int orderId);

    public List<Order> getOrdersByUser(int userId);

    public Order createOrder(OrderRequest orderRequest);

    public Order updateOrder(int orderId, OrderRequest orderRequest);

    public void deleteOrder(int orderId);

    public Order cancelOrder(int orderId);

    public Order updateStatus(int orderId, OrderStatus newStatus);

    public int cancelExpiredOrders();
}
