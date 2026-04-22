package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;

public interface DeliveryService {
    List<Delivery> getDeliveries();

    Delivery getDeliveryById(Integer deliveryId);

    List<Delivery> getDeliveriesByOrder(Integer orderId);

    Delivery createDelivery(DeliveryRequest request);

    Delivery updateDelivery(Integer deliveryId, DeliveryRequest request);

    void deleteDelivery(Integer deliveryId);
}
