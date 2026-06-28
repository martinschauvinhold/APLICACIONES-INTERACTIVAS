package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.PriceTierRequest;
import com.uade.tpo.demo.entity.dto.PriceTierResponse;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;
import com.uade.tpo.demo.entity.dto.ProductVariantResponse;
import com.uade.tpo.demo.entity.dto.VariantStockResponse;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.service.AuthorizationService;
import com.uade.tpo.demo.service.ProductService;
import com.uade.tpo.demo.service.ProductVariantService;

@RestController
@RequestMapping("variants")
public class ProductVariantsController {

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<List<ProductVariantResponse>> getVariants() {
        List<ProductVariantResponse> result = productVariantService.getVariants().stream()
                .map(ProductVariantResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ProductVariantResponse> getVariantById(@PathVariable int variantId) {
        Optional<ProductVariant> result = productVariantService.getVariantById(variantId);
        return result.map(v -> ResponseEntity.ok(ProductVariantResponse.from(v)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantResponse>> getVariantsByProduct(@PathVariable int productId) {
        List<ProductVariantResponse> result = productVariantService.getVariantsByProduct(productId).stream()
                .map(ProductVariantResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    // Stock agregado (suma de inventory en todos los depósitos) de lectura
    // pública: el detalle por depósito sigue restringido a seller/admin en /inventory.
    @GetMapping("/{variantId}/stock")
    public ResponseEntity<VariantStockResponse> getVariantStock(@PathVariable int variantId) {
        int stock = productVariantService.getStockByVariant(variantId);
        return ResponseEntity.ok(new VariantStockResponse(variantId, stock));
    }

    @GetMapping("/{variantId}/tiers")
    public ResponseEntity<List<PriceTierResponse>> getVariantTiers(@PathVariable int variantId) {
        List<PriceTierResponse> tiers = productVariantService.getTiersByVariant(variantId).stream()
                .map(t -> new PriceTierResponse(t.getId(), t.getMinQuantity(), t.getUnitPrice(), t.getCurrency()))
                .toList();
        return ResponseEntity.ok(tiers);
    }

    @PostMapping("/{variantId}/tiers")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Object> createTier(@PathVariable int variantId, @RequestBody PriceTierRequest tierRequest) {
        ProductVariant variant = productVariantService.getVariantById(variantId)
                .orElseThrow(() -> new NotFoundException("ProductVariant", variantId));
        requireOwnerOrAdmin(variant.getProduct());
        PriceTier result = productVariantService.createTier(variantId, tierRequest);
        return ResponseEntity.created(URI.create("/variants/" + variantId + "/tiers")).body(result);
    }

    @PutMapping("/{variantId}/tiers/{tierId}")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Object> updateTier(@PathVariable int variantId, @PathVariable int tierId,
                                              @RequestBody PriceTierRequest tierRequest) {
        Optional<PriceTier> existing = productVariantService.getTierById(tierId);
        if (existing.isEmpty() || existing.get().getVariant().getId() != variantId) {
            return ResponseEntity.notFound().build();
        }
        requireOwnerOrAdmin(existing.get().getVariant().getProduct());
        PriceTier updated = productVariantService.updateTier(tierId, tierRequest);
        return ResponseEntity.ok(updated);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Object> createVariant(@RequestBody ProductVariantRequest variantRequest) {
        Product product = productService.getProductById(variantRequest.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", variantRequest.getProductId()));
        requireOwnerOrAdmin(product);
        ProductVariant result = productVariantService.createVariant(variantRequest);
        return ResponseEntity.created(URI.create("/variants/" + result.getId())).body(ProductVariantResponse.from(result));
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Object> updateVariant(@PathVariable int variantId, @RequestBody ProductVariantRequest variantRequest) {
        Optional<ProductVariant> result = productVariantService.getVariantById(variantId);
        if (result.isPresent()) {
            requireOwnerOrAdmin(result.get().getProduct());
            ProductVariant updated = productVariantService.updateVariant(variantId, variantRequest);
            return ResponseEntity.ok(ProductVariantResponse.from(updated));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasAnyRole('seller', 'admin')")
    public ResponseEntity<Object> deleteVariant(@PathVariable int variantId) {
        Optional<ProductVariant> result = productVariantService.getVariantById(variantId);
        if (result.isPresent()) {
            requireOwnerOrAdmin(result.get().getProduct());
            productVariantService.deleteVariant(variantId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void requireOwnerOrAdmin(Product product) {
        if (product.getSeller() == null) {
            if (!authorizationService.isAdmin()) {
                throw new AccessDeniedException("Este producto no tiene vendedor asignado; solo un admin puede editarlo");
            }
            return;
        }
        authorizationService.requireSelfOrAdmin(product.getSeller().getId());
    }
}
