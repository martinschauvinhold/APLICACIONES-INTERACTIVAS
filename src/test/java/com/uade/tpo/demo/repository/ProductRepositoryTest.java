package com.uade.tpo.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.Role;
import com.uade.tpo.demo.entity.User;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager em;

    private User persistSeller(String username) {
        return em.persistAndFlush(User.builder()
                .username(username).email(username + "@test.com")
                .passwordHash("x").role(Role.seller).build());
    }

    // ─── existsByCategory_Id ──────────────────────────────────────────────────

    @Test
    void existsByCategory_Id_deberiaRetornarTrue_cuandoHayProductosEnLaCategoria() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var seller = persistSeller("seller_exists_true");
        em.persistAndFlush(Product.builder().name("iPhone 15").brand("Apple").category(category).seller(seller).build());

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

    // ─── search ───────────────────────────────────────────────────────────────

    @Test
    void search_deberiaDevolverSoloActivos_cuandoActiveEsTrue() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var seller = persistSeller("seller_active_true");
        em.persistAndFlush(Product.builder().name("iPhone 15").category(category).seller(seller).isActive(true).build());
        em.persistAndFlush(Product.builder().name("iPhone 14").category(category).seller(seller).isActive(false).build());

        // Act
        var result = productRepository.search(null, null, Boolean.TRUE, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName).containsExactly("iPhone 15");
    }

    @Test
    void search_conViewerNull_deberiaDevolverSoloActivos() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var seller = persistSeller("seller_viewer_null");
        em.persistAndFlush(Product.builder().name("Activo").category(category).seller(seller).isActive(true).build());
        em.persistAndFlush(Product.builder().name("Inactivo").category(category).seller(seller).isActive(false).build());

        // Act — sin viewer (público/buyer) los inactivos no son visibles
        var result = productRepository.search(null, null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName).containsExactly("Activo");
    }

    @Test
    void search_deberiaIncluirLosInactivosDelViewer_cuandoEsSuDueno() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var seller = persistSeller("seller_owner");
        em.persistAndFlush(Product.builder().name("Activo del seller").category(category).seller(seller).isActive(true).build());
        em.persistAndFlush(Product.builder().name("Inactivo del seller").category(category).seller(seller).isActive(false).build());

        // Act — el viewer es el dueño: ve sus propios inactivos
        var result = productRepository.search(null, null, null, null, seller.getId(), PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Activo del seller", "Inactivo del seller");
    }

    @Test
    void search_noDeberiaIncluirLosInactivosDeOtroSeller() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var otro = persistSeller("otro");
        var viewer = persistSeller("viewer");
        em.persistAndFlush(Product.builder().name("Activo de otro").category(category).seller(otro).isActive(true).build());
        em.persistAndFlush(Product.builder().name("Inactivo de otro").category(category).seller(otro).isActive(false).build());

        // Act — un vendedor no ve los inactivos de otro vendedor
        var result = productRepository.search(null, null, null, null, viewer.getId(), PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName).containsExactly("Activo de otro");
    }

    @Test
    void search_deberiaFiltrarPorCategoria() {
        // Arrange
        var smartphones = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var tablets = em.persistAndFlush(Category.builder().description("Tablets").build());
        var seller = persistSeller("seller_categoria");
        em.persistAndFlush(Product.builder().name("iPhone").category(smartphones).seller(seller).isActive(true).build());
        em.persistAndFlush(Product.builder().name("iPad").category(tablets).seller(seller).isActive(true).build());

        // Act
        var result = productRepository.search(smartphones.getId(), null, null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName).containsExactly("iPhone");
    }

    @Test
    void search_deberiaFiltrarPorSeller() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var seller = persistSeller("seller_filtro");
        var otro = persistSeller("otro_filtro");
        em.persistAndFlush(Product.builder().name("Del seller").category(category).seller(seller).isActive(true).build());
        em.persistAndFlush(Product.builder().name("De otro").category(category).seller(otro).isActive(true).build());

        // Act
        var result = productRepository.search(null, seller.getId(), null, null, null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName).containsExactly("Del seller");
    }

    @Test
    void search_deberiaBuscarPorNombreIgnorandoMayusculas() {
        // Arrange
        var category = em.persistAndFlush(Category.builder().description("Smartphones").build());
        var seller = persistSeller("seller_busqueda");
        em.persistAndFlush(Product.builder().name("Samsung Galaxy").category(category).seller(seller).isActive(true).build());
        em.persistAndFlush(Product.builder().name("iPhone 15").category(category).seller(seller).isActive(true).build());

        // Act
        var result = productRepository.search(null, null, null, "galaxy", null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).extracting(Product::getName).containsExactly("Samsung Galaxy");
    }
}
