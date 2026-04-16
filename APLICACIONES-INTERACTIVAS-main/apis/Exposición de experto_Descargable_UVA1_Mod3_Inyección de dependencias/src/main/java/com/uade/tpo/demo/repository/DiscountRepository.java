package com.uade.tpo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Discount;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    List<Discount> findByIsActive(boolean isActive);

    List<Discount> findByProductIdAndIsActiveTrue(int productId);

    List<Discount> findByCategoryIdAndIsActiveTrue(int categoryId);
}
