package com.uade.tpo.demo.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCode(String code);

    /**
     * Igual que findByCode pero con lock pesimista, para incrementar timesUsed
     * sin condición de carrera: si dos compras con el mismo cupón llegan casi
     * juntas, una espera a que la otra confirme su incremento antes de leer
     * el contador, así ninguna pasa por alto el usageLimit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.code = :code")
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
}
