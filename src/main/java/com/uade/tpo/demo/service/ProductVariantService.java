package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;

public interface ProductVariantService {
    ArrayList<ProductVariant> getVariants();

    Optional<ProductVariant> getVariantById(int variantId);

    List<ProductVariant> getVariantsByProduct(int productId);

    ProductVariant createVariant(ProductVariantRequest variantRequest);

    ProductVariant updateVariant(int variantId, ProductVariantRequest variantRequest);

    void deleteVariant(int variantId);

    int getStockByVariant(int variantId);

    List<PriceTier> getTiersByVariant(int variantId);
}
