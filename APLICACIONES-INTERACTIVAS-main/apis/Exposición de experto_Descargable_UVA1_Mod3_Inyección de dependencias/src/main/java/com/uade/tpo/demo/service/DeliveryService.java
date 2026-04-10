package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.dto.DeliveryRequest;

public interface DeliveryService {
    public ArrayList<Delivery> getDeliveries();

    public Optional<Delivery> getDeliveryById(int deliveryId);

    public List<Delivery> getDeliveriesByOrder(int orderId);

    public Delivery createDelivery(DeliveryRequest deliveryRequest);

    public Delivery updateDelivery(int deliveryId, DeliveryRequest deliveryRequest);

    public void deleteDelivery(int deliveryId);
}
