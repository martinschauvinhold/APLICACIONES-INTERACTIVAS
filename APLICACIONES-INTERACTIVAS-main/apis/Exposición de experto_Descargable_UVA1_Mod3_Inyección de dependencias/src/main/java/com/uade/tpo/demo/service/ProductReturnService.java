package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;

public interface ProductReturnService {
    public ArrayList<ProductReturn> getReturns();

    public Optional<ProductReturn> getReturnById(int returnId);

    public List<ProductReturn> getReturnsByOrder(int orderId);

    public ProductReturn createReturn(ProductReturnRequest returnRequest);

    public ProductReturn updateReturn(int returnId, ProductReturnRequest returnRequest);

    public void deleteReturn(int returnId);
}
