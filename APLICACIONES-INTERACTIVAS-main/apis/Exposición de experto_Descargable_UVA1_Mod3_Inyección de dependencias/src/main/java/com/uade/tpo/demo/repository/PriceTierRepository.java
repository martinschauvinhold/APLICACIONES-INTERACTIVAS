package com.uade.tpo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.PriceTier;

@Repository
public interface PriceTierRepository extends JpaRepository<PriceTier, Integer> {
    List<PriceTier> findByVariantId(int variantId);
}
