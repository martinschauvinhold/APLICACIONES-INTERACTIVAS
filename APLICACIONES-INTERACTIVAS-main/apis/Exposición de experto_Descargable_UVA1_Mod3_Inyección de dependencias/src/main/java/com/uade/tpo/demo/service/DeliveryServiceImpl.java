package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.OrderRepository;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    public ArrayList<Delivery> getDeliveries() {
        return new ArrayList<>(deliveryRepository.findAll());
    }

    public Optional<Delivery> getDeliveryById(int deliveryId) {
        return deliveryRepository.findById(deliveryId);
    }

    public List<Delivery> getDeliveriesByOrder(int orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }

    public Delivery createDelivery(DeliveryRequest deliveryRequest) {
        Order order = orderRepository.findById(deliveryRequest.getOrderId()).get();
        Delivery delivery = Delivery.builder()
                .order(order)
                .shippingMethod(deliveryRequest.getShippingMethod())
                .trackingNumber(deliveryRequest.getTrackingNumber())
                .deliveryStatus(deliveryRequest.getDeliveryStatus())
                .dispatchedAt(new Date())
                .build();
        return deliveryRepository.save(delivery);
    }

    public Delivery updateDelivery(int deliveryId, DeliveryRequest deliveryRequest) {
        Delivery delivery = deliveryRepository.findById(deliveryId).get();
        delivery.setShippingMethod(deliveryRequest.getShippingMethod());
        delivery.setTrackingNumber(deliveryRequest.getTrackingNumber());
        delivery.setDeliveryStatus(deliveryRequest.getDeliveryStatus());
        return deliveryRepository.save(delivery);
    }

    public void deleteDelivery(int deliveryId) {
        deliveryRepository.deleteById(deliveryId);
    }
}
