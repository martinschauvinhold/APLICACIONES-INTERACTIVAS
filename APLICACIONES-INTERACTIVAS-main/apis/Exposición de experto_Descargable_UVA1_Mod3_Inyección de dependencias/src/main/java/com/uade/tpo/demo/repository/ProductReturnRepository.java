package com.uade.tpo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.ProductReturn;

@Repository
public interface ProductReturnRepository extends JpaRepository<ProductReturn, Integer> {
    List<ProductReturn> findByOrderId(int orderId);
}
