package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<OrderItem> getItemsByOrder(int orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Optional<OrderItem> getItemById(int itemId) {
        return orderItemRepository.findById(itemId);
    }

    public void deleteItem(int itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("OrderItem", itemId));
        Order order = item.getOrder();

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException(
                    "Solo se pueden borrar items de ordenes en estado PENDING. Estado actual: "
                            + order.getStatus());
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        if (items.size() <= 1) {
            throw new BusinessRuleException(
                    "No se puede borrar el ultimo item de una orden. Cancelar la orden en su lugar.");
        }

        orderItemRepository.deleteById(itemId);
        recalculateOrderTotal(order);
    }

    private void recalculateOrderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        order.setUpdatedAt(new Date());
        orderRepository.save(order);
    }
}
