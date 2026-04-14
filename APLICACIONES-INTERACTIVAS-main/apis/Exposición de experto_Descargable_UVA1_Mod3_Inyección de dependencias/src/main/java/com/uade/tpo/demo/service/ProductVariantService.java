package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;

public interface ProductVariantService {
    public ArrayList<ProductVariant> getVariants();

    public Optional<ProductVariant> getVariantById(int variantId);

    public List<ProductVariant> getVariantsByProduct(int productId);

    public ProductVariant createVariant(ProductVariantRequest variantRequest);

    public ProductVariant updateVariant(int variantId, ProductVariantRequest variantRequest);

    public void deleteVariant(int variantId);
}
