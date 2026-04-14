package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.ShipmentTracking;
import com.uade.tpo.demo.entity.TrackingStatus;
import com.uade.tpo.demo.entity.dto.ShipmentTrackingRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.ShipmentTrackingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentTrackingServiceImpl implements ShipmentTrackingService {

    private final ShipmentTrackingRepository trackingRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public List<ShipmentTracking> getByDeliveryId(Integer deliveryId) {
        if (!deliveryRepository.existsById(deliveryId))
            throw new NotFoundException("Delivery", deliveryId);
        return trackingRepository.findByDeliveryId(deliveryId);
    }

    @Override
    public ShipmentTracking getById(Integer trackingId) {
        return trackingRepository.findById(trackingId)
                .orElseThrow(() -> new NotFoundException("ShipmentTracking", trackingId));
    }

    @Override
    @Transactional
    public ShipmentTracking addCheckpoint(ShipmentTrackingRequest request) {
        var delivery = deliveryRepository.findById(request.deliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery", request.deliveryId()));
        var checkpoint = ShipmentTracking.builder()
                .delivery(delivery)
                .checkpoint(request.checkpoint())
                .status(request.status())
                .recordedAt(new Date())
                .build();
        return trackingRepository.save(checkpoint);
    }

    @Override
    @Transactional
    public ShipmentTracking updateStatus(Integer trackingId, TrackingStatus status) {
        var checkpoint = trackingRepository.findById(trackingId)
                .orElseThrow(() -> new NotFoundException("ShipmentTracking", trackingId));
        checkpoint.setStatus(status);
        return trackingRepository.save(checkpoint);
    }
}
