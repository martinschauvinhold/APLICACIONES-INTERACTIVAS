package com.uade.tpo.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {
    List<Refund> findByProductReturnId(int productReturnId);
}
