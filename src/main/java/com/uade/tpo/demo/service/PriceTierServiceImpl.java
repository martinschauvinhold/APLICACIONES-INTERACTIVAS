package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.PriceTierRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.PriceTierRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;

@Service
public class PriceTierServiceImpl implements PriceTierService {

    @Autowired
    private PriceTierRepository priceTierRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    public List<PriceTier> getTiersByVariant(int variantId) {
        return priceTierRepository.findByVariantId(variantId);
    }

    public Optional<PriceTier> getTierById(int tierId) {
        return priceTierRepository.findById(tierId);
    }

    public BigDecimal getEffectivePrice(int variantId, int quantity) {
        List<PriceTier> tiers = priceTierRepository.findByVariantId(variantId);
        return tiers.stream()
                .filter(t -> t.getMinQuantity() <= quantity)
                .max(Comparator.comparingInt(PriceTier::getMinQuantity))
                .map(PriceTier::getUnitPrice)
                .orElseGet(() -> {
                    ProductVariant variant = productVariantRepository.findById(variantId)
                            .orElseThrow(() -> new NotFoundException("Variant", variantId));
                    return variant.getBasePrice();
                });
    }

    public PriceTier createTier(PriceTierRequest request) {
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new NotFoundException("Variant", request.getVariantId()));
        PriceTier tier = PriceTier.builder()
                .variant(variant)
                .minQuantity(request.getMinQuantity())
                .unitPrice(request.getUnitPrice())
                .build();
        return priceTierRepository.save(tier);
    }

    public void deleteTier(int tierId) {
        if (!priceTierRepository.existsById(tierId))
            throw new NotFoundException("PriceTier", tierId);
        priceTierRepository.deleteById(tierId);
    }
}