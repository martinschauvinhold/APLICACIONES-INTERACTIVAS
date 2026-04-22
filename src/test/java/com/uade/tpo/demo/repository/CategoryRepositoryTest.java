package com.uade.tpo.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.uade.tpo.demo.entity.Category;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager em;

    // ─── findByDescription ────────────────────────────────────────────────────

    @Test
    void findByDescription_deberiaRetornarCategoria_cuandoDescripcionCoincide() {
        // Arrange
        em.persistAndFlush(Category.builder().description("Smartphones").build());

        // Act
        var result = categoryRepository.findByDescription("Smartphones");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Smartphones");
    }

    @Test
    void findByDescription_deberiaRetornarListaVacia_cuandoDescripcionNoCoincide() {
        // Arrange
        em.persistAndFlush(Category.builder().description("Notebooks").build());

        // Act
        var result = categoryRepository.findByDescription("Tablets");

        // Assert
        assertThat(result).isEmpty();
    }

    // ─── findByIsActiveTrue ───────────────────────────────────────────────────

    @Test
    void findByIsActiveTrue_deberiaRetornarSoloLasActivas() {
        // Arrange
        em.persistAndFlush(Category.builder().description("Smartphones").isActive(true).build());
        em.persistAndFlush(Category.builder().description("Descontinuados").isActive(false).build());

        // Act
        var result = categoryRepository.findByIsActiveTrue();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Smartphones");
    }

    @Test
    void findByIsActiveTrue_deberiaRetornarListaVacia_cuandoNoHayActivas() {
        // Arrange
        em.persistAndFlush(Category.builder().description("Descontinuados").isActive(false).build());

        // Act
        var result = categoryRepository.findByIsActiveTrue();

        // Assert
        assertThat(result).isEmpty();
    }
}
