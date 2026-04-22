package com.uade.tpo.demo.repository;

import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    List<Inventory> findByVariantId(int variantId);

    List<Inventory> findByWarehouseId(int warehouseId);

    /**
     * Busca Inventory rows de una variante con pessimistic write lock.
     * Usado al procesar el pago para evitar race conditions de stock:
     * si dos transacciones intentan comprar el mismo producto, una espera
     * a que la otra termine antes de leer el stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.variant.id = :variantId")
    List<Inventory> findByVariantIdForUpdate(@Param("variantId") int variantId);
}
