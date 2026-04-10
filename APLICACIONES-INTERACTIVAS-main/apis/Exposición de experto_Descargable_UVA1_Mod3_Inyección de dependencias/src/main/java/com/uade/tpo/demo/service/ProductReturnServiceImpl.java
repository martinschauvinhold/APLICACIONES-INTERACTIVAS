package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductReturnRepository;

@Service
public class ProductReturnServiceImpl implements ProductReturnService {

    @Autowired
    private ProductReturnRepository productReturnRepository;

    @Autowired
    private OrderRepository orderRepository;

    public ArrayList<ProductReturn> getReturns() {
        return new ArrayList<>(productReturnRepository.findAll());
    }

    public Optional<ProductReturn> getReturnById(int returnId) {
        return productReturnRepository.findById(returnId);
    }

    public List<ProductReturn> getReturnsByOrder(int orderId) {
        return productReturnRepository.findByOrderId(orderId);
    }

    public ProductReturn createReturn(ProductReturnRequest returnRequest) {
        Order order = orderRepository.findById(returnRequest.getOrderId()).get();
        ProductReturn productReturn = ProductReturn.builder()
                .order(order)
                .reason(returnRequest.getReason())
                .status(returnRequest.getStatus())
                .requestedAt(new Date())
                .build();
        return productReturnRepository.save(productReturn);
    }

    public ProductReturn updateReturn(int returnId, ProductReturnRequest returnRequest) {
        ProductReturn productReturn = productReturnRepository.findById(returnId).get();
        productReturn.setReason(returnRequest.getReason());
        productReturn.setStatus(returnRequest.getStatus());
        return productReturnRepository.save(productReturn);
    }

    public void deleteReturn(int returnId) {
        productReturnRepository.deleteById(returnId);
    }
}
