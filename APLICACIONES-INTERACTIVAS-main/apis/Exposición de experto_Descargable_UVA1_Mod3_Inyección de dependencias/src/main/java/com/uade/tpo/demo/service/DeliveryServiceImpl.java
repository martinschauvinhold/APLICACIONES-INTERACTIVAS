package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.DeliveryStatus;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ShipmentTrackingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;

    @Override
    public List<Delivery> getDeliveries() {
        return deliveryRepository.findAll();
    }

    @Override
    public Delivery getDeliveryById(Integer deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));
    }

    @Override
    public List<Delivery> getDeliveriesByOrder(Integer orderId) {
        if (!orderRepository.existsById(orderId))
            throw new NotFoundException("Order", orderId);
        return deliveryRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Delivery createDelivery(DeliveryRequest request) {
        var order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order", request.orderId()));
        var delivery = Delivery.builder()
                .order(order)
                .shippingMethod(request.shippingMethod())
                .trackingNumber(request.trackingNumber())
                .status(request.status() != null ? request.status() : DeliveryStatus.PENDING)
                .dispatchedAt(new Date())
                .build();
        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery updateDelivery(Integer deliveryId, DeliveryRequest request) {
        var delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));
        delivery.setShippingMethod(request.shippingMethod());
        delivery.setTrackingNumber(request.trackingNumber());
        delivery.setStatus(request.status());
        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public void deleteDelivery(Integer deliveryId) {
        if (!deliveryRepository.existsById(deliveryId))
            throw new NotFoundException("Delivery", deliveryId);
        shipmentTrackingRepository.deleteAll(shipmentTrackingRepository.findByDeliveryId(deliveryId));
        deliveryRepository.deleteById(deliveryId);
    }
}
