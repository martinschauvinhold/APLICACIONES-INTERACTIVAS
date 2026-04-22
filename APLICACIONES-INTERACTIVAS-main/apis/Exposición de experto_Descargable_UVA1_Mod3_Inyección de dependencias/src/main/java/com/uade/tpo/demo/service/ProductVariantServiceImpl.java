package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    public ArrayList<ProductVariant> getVariants() {
        return new ArrayList<>(productVariantRepository.findAll());
    }

    public Optional<ProductVariant> getVariantById(int variantId) {
        return productVariantRepository.findById(variantId);
    }

    public List<ProductVariant> getVariantsByProduct(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product", productId);
        }
        return productVariantRepository.findByProductId(productId);
    }

    public ProductVariant createVariant(ProductVariantRequest variantRequest) {
        Product product = productRepository.findById(variantRequest.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", variantRequest.getProductId()));
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(variantRequest.getSku())
                .attributes(variantRequest.getAttributes())
                .basePrice(variantRequest.getBasePrice())
                .updatedAt(new Date())
                .build();
        return productVariantRepository.save(variant);
    }

    public ProductVariant updateVariant(int variantId, ProductVariantRequest variantRequest) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ProductVariant", variantId));
        variant.setSku(variantRequest.getSku());
        variant.setAttributes(variantRequest.getAttributes());
        variant.setBasePrice(variantRequest.getBasePrice());
        variant.setUpdatedAt(new Date());
        return productVariantRepository.save(variant);
    }

    public void deleteVariant(int variantId) {
        productVariantRepository.deleteById(variantId);
    }
}
