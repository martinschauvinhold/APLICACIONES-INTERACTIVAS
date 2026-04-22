package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.DeliveryStatus;
import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.ReturnStatus;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductReturnRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReturnServiceImpl implements ProductReturnService {

    private final ProductReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public List<ProductReturn> getReturns() {
        return returnRepository.findAll();
    }

    @Override
    public ProductReturn getReturnById(Integer returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new NotFoundException("ProductReturn", returnId));
    }

    @Override
    public List<ProductReturn> getReturnsByOrder(Integer orderId) {
        return returnRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public ProductReturn createReturn(ProductReturnRequest request) {
        var order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order", request.orderId()));

        boolean hasDispatchedDelivery = deliveryRepository.findByOrderId(request.orderId())
                .stream()
                .anyMatch(d -> d.getStatus() != DeliveryStatus.PENDING);

        if (!hasDispatchedDelivery)
            throw new BusinessRuleException(
                    "Cannot create a return: the order has no dispatched delivery");

        var productReturn = ProductReturn.builder()
                .order(order)
                .reason(request.reason())
                .status(request.status() != null ? request.status() : ReturnStatus.PENDING)
                .requestedAt(new Date())
                .build();
        return returnRepository.save(productReturn);
    }

    @Override
    @Transactional
    public ProductReturn updateReturn(Integer returnId, ProductReturnRequest request) {
        var productReturn = returnRepository.findById(returnId)
                .orElseThrow(() -> new NotFoundException("ProductReturn", returnId));
        productReturn.setReason(request.reason());
        productReturn.setStatus(request.status());
        return returnRepository.save(productReturn);
    }

    @Override
    @Transactional
    public void deleteReturn(Integer returnId) {
        if (!returnRepository.existsById(returnId))
            throw new NotFoundException("ProductReturn", returnId);
        returnRepository.deleteById(returnId);
    }
}
