package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uade.tpo.demo.entity.ProductImage;
import com.uade.tpo.demo.entity.dto.ProductImageRequest;
import com.uade.tpo.demo.service.ProductImageService;

@RestController
@RequestMapping("product-images")
public class ProductImagesController {

    @Autowired
    private ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImage>> getImagesByProduct(@PathVariable int productId) {
        return ResponseEntity.ok(productImageService.getImagesByProduct(productId));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ProductImage> getImageById(@PathVariable int imageId) {
        Optional<ProductImage> result = productImageService.getImageById(imageId);
        if (result.isPresent()) return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Object> createImage(@RequestBody ProductImageRequest request) {
        ProductImage result = productImageService.createImage(request);
        return ResponseEntity.created(URI.create("/product-images/" + result.getId())).body(result);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Object> deleteImage(@PathVariable int imageId) {
        Optional<ProductImage> result = productImageService.getImageById(imageId);
        if (result.isPresent()) { productImageService.deleteImage(imageId); return ResponseEntity.noContent().build(); }
        return ResponseEntity.notFound().build();
    }
}