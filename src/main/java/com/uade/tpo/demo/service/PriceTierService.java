package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.dto.PriceTierRequest;

public interface PriceTierService {
    List<PriceTier> getTiersByVariant(int variantId);
    Optional<PriceTier> getTierById(int tierId);
    BigDecimal getEffectivePrice(int variantId, int quantity);
    PriceTier createTier(PriceTierRequest request);
    void deleteTier(int tierId);
}