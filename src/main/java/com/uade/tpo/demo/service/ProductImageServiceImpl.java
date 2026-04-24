package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.dto.ProductImageRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.ProductImageRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductImage> getImagesByProduct(int productId) {
        return productImageRepository.findByProductId(productId);
    }

    public Optional<ProductImage> getImageById(int imageId) {
        return productImageRepository.findById(imageId);
    }

    public ProductImage createImage(ProductImageRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));
        ProductImage image = ProductImage.builder()
                .product(product)
                .url(request.getUrl())
                .isPrimary(request.isPrimary())
                .sortOrder(request.getSortOrder())
                .build();
        return productImageRepository.save(image);
    }

    public void deleteImage(int imageId) {
        if (!productImageRepository.existsById(imageId))
            throw new NotFoundException("ProductImage", imageId);
        productImageRepository.deleteById(imageId);
    }
}