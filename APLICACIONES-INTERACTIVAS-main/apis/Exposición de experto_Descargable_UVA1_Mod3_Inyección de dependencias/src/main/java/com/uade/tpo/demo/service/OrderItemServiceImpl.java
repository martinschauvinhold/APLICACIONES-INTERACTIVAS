package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.OrderItemRequest;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    public List<OrderItem> getItemsByOrder(int orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Optional<OrderItem> getItemById(int itemId) {
        return orderItemRepository.findById(itemId);
    }

    public OrderItem addItem(OrderItemRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found: " + request.getVariantId()));

        BigDecimal unitPrice = variant.getBasePrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        OrderItem item = OrderItem.builder()
                .order(order)
                .variant(variant)
                .quantity(request.getQuantity())
                .unitPriceAtTime(unitPrice)
                .discountApplied(BigDecimal.ZERO)
                .subtotal(subtotal)
                .build();

        OrderItem saved = orderItemRepository.save(item);

        recalculateOrderTotal(order);

        return saved;
    }

    public void deleteItem(int itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("OrderItem not found: " + itemId));
        Order order = item.getOrder();
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
