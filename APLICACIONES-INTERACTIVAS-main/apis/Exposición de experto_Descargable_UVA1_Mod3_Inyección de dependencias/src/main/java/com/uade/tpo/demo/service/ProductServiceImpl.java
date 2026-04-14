package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public ArrayList<Product> getProducts() {
        return new ArrayList<>(productRepository.findAll());
    }

    public Optional<Product> getProductById(int productId) {
        return productRepository.findById(productId);
    }

    public Product createProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", productRequest.getCategoryId()));
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .brand(productRequest.getBrand())
                .category(category)
                .isActive(true)
                .updatedAt(new Date())
                .build();
        return productRepository.save(product);
    }

    public Product updateProduct(int productId, ProductRequest productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", productId));
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", productRequest.getCategoryId()));
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setBrand(productRequest.getBrand());
        product.setCategory(category);
        product.setUpdatedAt(new Date());
        return productRepository.save(product);
    }

    public void deleteProduct(int productId) {
        productRepository.deleteById(productId);
    }
}
