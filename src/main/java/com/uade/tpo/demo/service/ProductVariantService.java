package com.uade.tpo.demo.service;

import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.PriceTierRequest;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;
import com.uade.tpo.demo.entity.dto.ProductVariantResponse;

public interface ProductVariantService {
    List<ProductVariantResponse> getVariants();

    Optional<ProductVariant> getVariantById(int variantId);

    List<ProductVariantResponse> getVariantsByProduct(int productId);

    ProductVariant createVariant(ProductVariantRequest variantRequest);

    ProductVariant updateVariant(int variantId, ProductVariantRequest variantRequest);

    void deleteVariant(int variantId);

    int getStockByVariant(int variantId);

    List<PriceTier> getTiersByVariant(int variantId);

    Optional<PriceTier> getTierById(int tierId);

    PriceTier createTier(int variantId, PriceTierRequest tierRequest);

    PriceTier updateTier(int tierId, PriceTierRequest tierRequest);
}
