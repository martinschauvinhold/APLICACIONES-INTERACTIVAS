package com.uade.tpo.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager em;

    // ─── existsByCategory_Id ──────────────────────────────────────────────────

    @Test
    void existsByCategory_Id_deberiaRetornarTrue_cuandoHayProductosEnLaCategoria() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        em.persistAndFlush(Product.builder().name("iPhone 15").brand("Apple").category(category).build());

        // Act
        var result = productRepository.existsByCategory_Id(category.getId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void existsByCategory_Id_deberiaRetornarFalse_cuandoLaCategoriaNoTieneProductos() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Accesorios").build());

        // Act
        var result = productRepository.existsByCategory_Id(category.getId());

        // Assert
        assertThat(result).isFalse();
    }
}
