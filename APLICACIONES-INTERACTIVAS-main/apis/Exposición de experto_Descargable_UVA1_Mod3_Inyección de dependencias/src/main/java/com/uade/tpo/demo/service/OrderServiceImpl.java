package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Address;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.OrderRequest;
import com.uade.tpo.demo.repository.AddressRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    public ArrayList<Order> getOrders() {
        return new ArrayList<>(orderRepository.findAll());
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByUser(int userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order createOrder(OrderRequest orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId()).get();
        Address address = addressRepository.findById(orderRequest.getShippingAddressId()).get();
        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .status(orderRequest.getStatus())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        return orderRepository.save(order);
    }

    public Order updateOrder(int orderId, OrderRequest orderRequest) {
        Order order = orderRepository.findById(orderId).get();
        order.setStatus(orderRequest.getStatus());
        order.setUpdatedAt(new Date());
        return orderRepository.save(order);
    }

    public void deleteOrder(int orderId) {
        orderRepository.deleteById(orderId);
    }
}
