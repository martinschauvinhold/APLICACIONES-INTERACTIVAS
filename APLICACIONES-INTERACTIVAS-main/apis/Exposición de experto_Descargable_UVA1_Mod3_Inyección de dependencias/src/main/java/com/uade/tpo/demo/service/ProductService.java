package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.ProductRequest;

public interface ProductService {
    public ArrayList<Product> getProducts();

    public List<Product> searchProducts(Integer categoryId, String brand, String name);

    public Optional<Product> getProductById(int productId);

    public Product createProduct(ProductRequest productRequest);

    public Product updateProduct(int productId, ProductRequest productRequest);

    public void deleteProduct(int productId);
}
