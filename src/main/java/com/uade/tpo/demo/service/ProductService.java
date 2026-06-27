package com.uade.tpo.demo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.entity.dto.ProductResponse;

public interface ProductService {
    public Page<ProductResponse> getProducts(Integer categoryId, Integer sellerId, String search, Boolean active,
            Integer viewerSellerId, Pageable pageable);

    public Optional<Product> getProductById(int productId);

    public Product createProduct(ProductRequest productRequest);

    public Product updateProduct(int productId, ProductRequest productRequest);

    public void deleteProduct(int productId);
}
