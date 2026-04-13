package com.uade.tpo.demo.service;

import java.util.List;

import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;

public interface ProductReturnService {
    List<ProductReturn> getReturns();

    ProductReturn getReturnById(Integer returnId);

    List<ProductReturn> getReturnsByOrder(Integer orderId);

    ProductReturn createReturn(ProductReturnRequest request);

    ProductReturn updateReturn(Integer returnId, ProductReturnRequest request);

    void deleteReturn(Integer returnId);
}
