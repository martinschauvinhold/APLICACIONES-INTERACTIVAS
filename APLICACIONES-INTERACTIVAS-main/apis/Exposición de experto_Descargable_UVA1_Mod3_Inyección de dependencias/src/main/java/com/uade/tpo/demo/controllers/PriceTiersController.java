package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.PriceTier;
import com.uade.tpo.demo.entity.dto.PriceTierRequest;
import com.uade.tpo.demo.service.PriceTierService;

@RestController
@RequestMapping("price-tiers")
public class PriceTiersController {

    @Autowired
    private PriceTierService priceTierService;

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<PriceTier>> getTiersByVariant(@PathVariable int variantId) {
        return ResponseEntity.ok(priceTierService.getTiersByVariant(variantId));
    }

    @GetMapping("/{tierId}")
    public ResponseEntity<PriceTier> getTierById(@PathVariable int tierId) {
        Optional<PriceTier> result = priceTierService.getTierById(tierId);
        if (result.isPresent())
            return ResponseEntity.ok(result.get());
        return ResponseEntity.noContent().build();
    }

    /**
     * Devuelve el precio efectivo para una variante dado una cantidad.
     * Ejemplo: GET /price-tiers/variant/1/price?quantity=10
     */
    @GetMapping("/variant/{variantId}/price")
    public ResponseEntity<BigDecimal> getEffectivePrice(
            @PathVariable int variantId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(priceTierService.getEffectivePrice(variantId, quantity));
    }

    @PostMapping
    public ResponseEntity<Object> createTier(@RequestBody PriceTierRequest request) {
        PriceTier result = priceTierService.createTier(request);
        return ResponseEntity.created(URI.create("/price-tiers/" + result.getId())).body(result);
    }

    @DeleteMapping("/{tierId}")
    public ResponseEntity<Object> deleteTier(@PathVariable int tierId) {
        Optional<PriceTier> result = priceTierService.getTierById(tierId);
        if (result.isPresent()) {
            priceTierService.deleteTier(tierId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
