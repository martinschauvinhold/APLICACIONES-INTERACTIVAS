package com.uade.tpo.demo.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.PriceTierRequest;
import com.uade.tpo.demo.entity.dto.PriceTierResponse;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;
import com.uade.tpo.demo.entity.dto.ProductVariantResponse;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.PriceTierRepository;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;

@Service
public class ProductVariantServiceImpl implements ProductVariantService {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PriceTierRepository priceTierRepository;

    public List<ProductVariantResponse> getVariants() {
        return hydrate(productVariantRepository.findAll());
    }

    public Optional<ProductVariant> getVariantById(int variantId) {
        return productVariantRepository.findById(variantId);
    }

    public List<ProductVariantResponse> getVariantsByProduct(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product", productId);
        }
        return hydrate(productVariantRepository.findByProductId(productId));
    }

    /**
     * Arma la respuesta de un listado de variantes con su stock y tiers ya
     * calculados en lote (2 consultas en total, no 2 por variante), para no
     * incurrir en N+1 al traer el catálogo completo.
     */
    private List<ProductVariantResponse> hydrate(List<ProductVariant> variants) {
        if (variants.isEmpty()) {
            return List.of();
        }
        List<Integer> variantIds = variants.stream().map(ProductVariant::getId).toList();

        Map<Integer, Integer> stockByVariant = inventoryRepository.findByVariantIdIn(variantIds).stream()
                .collect(Collectors.groupingBy(i -> i.getVariant().getId(),
                        Collectors.summingInt(Inventory::getStockQuantity)));

        Map<Integer, List<PriceTierResponse>> tiersByVariant = priceTierRepository.findByVariantIdIn(variantIds)
                .stream()
                .collect(Collectors.groupingBy(t -> t.getVariant().getId(),
                        Collectors.mapping(
                                t -> new PriceTierResponse(t.getId(), t.getMinQuantity(), t.getUnitPrice(), t.getCurrency()),
                                Collectors.toList())));

        return variants.stream()
                .map(v -> ProductVariantResponse.from(v, stockByVariant.getOrDefault(v.getId(), 0),
                        tiersByVariant.getOrDefault(v.getId(), List.of())))
                .toList();
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

    public int getStockByVariant(int variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new NotFoundException("ProductVariant", variantId);
        }
        return inventoryRepository.findByVariantId(variantId).stream()
                .mapToInt(Inventory::getStockQuantity)
                .sum();
    }

    public List<PriceTier> getTiersByVariant(int variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new NotFoundException("ProductVariant", variantId);
        }
        return priceTierRepository.findByVariantId(variantId);
    }

    public Optional<PriceTier> getTierById(int tierId) {
        return priceTierRepository.findById(tierId);
    }

    public PriceTier createTier(int variantId, PriceTierRequest tierRequest) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("ProductVariant", variantId));
        PriceTier tier = PriceTier.builder()
                .variant(variant)
                .minQuantity(tierRequest.getMinQuantity())
                .unitPrice(tierRequest.getUnitPrice())
                .build();
        return priceTierRepository.save(tier);
    }

    public PriceTier updateTier(int tierId, PriceTierRequest tierRequest) {
        PriceTier tier = priceTierRepository.findById(tierId)
                .orElseThrow(() -> new NotFoundException("PriceTier", tierId));
        tier.setMinQuantity(tierRequest.getMinQuantity());
        tier.setUnitPrice(tierRequest.getUnitPrice());
        return priceTierRepository.save(tier);
    }
}
