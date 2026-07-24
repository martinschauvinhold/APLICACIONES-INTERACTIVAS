package com.uade.tpo.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByCategory_Id(int categoryId);

    /**
     * Listado paginado con filtros opcionales (cada filtro null = no aplica).
     * Visibilidad a nivel fila: los productos inactivos solo los ve su dueño —
     * un caller vendedor (viewerSellerId) ve además sus propios inactivos; el
     * resto solo ve activos. El filtro `active` nunca amplía la visibilidad
     * (un buyer pidiendo active=false no ve los inactivos de nadie).
     */
    @Query("""
            SELECT p FROM Product p
            LEFT JOIN p.category c
            LEFT JOIN p.seller s
            WHERE (:categoryId IS NULL OR c.id = :categoryId)
              AND (:sellerId IS NULL OR s.id = :sellerId)
              AND (:active IS NULL OR p.isActive = :active)
              AND (p.isActive = TRUE OR s.id = :viewerSellerId)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Product> search(
            @Param("categoryId") Integer categoryId,
            @Param("sellerId") Integer sellerId,
            @Param("active") Boolean active,
            @Param("search") String search,
            @Param("viewerSellerId") Integer viewerSellerId,
            Pageable pageable);
}
