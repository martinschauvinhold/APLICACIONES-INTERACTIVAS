package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.dto.ProductImageRequest;

public interface ProductImageService {
    List<ProductImage> getImagesByProduct(int productId);
    Optional<ProductImage> getImageById(int imageId);
    ProductImage createImage(ProductImageRequest request);
    void deleteImage(int imageId);
}
