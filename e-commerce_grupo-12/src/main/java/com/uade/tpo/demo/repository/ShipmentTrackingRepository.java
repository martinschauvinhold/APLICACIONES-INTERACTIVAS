package com.uade.tpo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.ShipmentTracking;

@Repository
public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Integer> {
    List<ShipmentTracking> findByDeliveryId(int deliveryId);
}
