package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.DuplicateException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ─── getCategories ────────────────────────────────────────────────────────

    @Test
    void getCategories_deberiaRetornarListaCompleta() {
        // Arrange
        var categories = List.of(
                Category.builder().id(1).description("Smartphones").build(),
                Category.builder().id(2).description("Notebooks").build());
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        var result = categoryService.getCategories();

        // Assert
        assertThat(result).hasSize(2);
    }

    // ─── getActiveCategories ──────────────────────────────────────────────────

    @Test
    void getActiveCategories_deberiaRetornarSoloLasActivas() {
        // Arrange
        var active = Category.builder().id(1).description("Smartphones").isActive(true).build();
        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(active));

        // Act
        var result = categoryService.getActiveCategories();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    // ─── getCategoryById ──────────────────────────────────────────────────────

    @Test
    void getCategoryById_deberiaRetornarCategoria_cuandoIdExiste() {
        // Arrange
        var category = Category.builder().id(1).description("Smartphones").build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        // Act
        var result = categoryService.getCategoryById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Smartphones");
    }

    @Test
    void getCategoryById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = categoryService.getCategoryById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    // ─── createCategory ───────────────────────────────────────────────────────

    @Test
    void createCategory_deberiaGuardarYRetornarCategoria_cuandoDescripcionNoDuplicada() {
        // Arrange
        when(categoryRepository.findByDescription("Tablets")).thenReturn(List.of());
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = categoryService.createCategory("Tablets", null);

        // Assert
        assertThat(result.getDescription()).isEqualTo("Tablets");
        verify(categoryRepository).save(any());
    }

    @Test
    void createCategory_deberiaLanzarDuplicateException_cuandoDescripcionYaExiste() {
        // Arrange
        var existing = Category.builder().id(1).description("Smartphones").build();
        when(categoryRepository.findByDescription("Smartphones")).thenReturn(List.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory("Smartphones", null))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("Smartphones");
    }

    // ─── updateCategory ───────────────────────────────────────────────────────

    @Test
    void updateCategory_deberiaActualizarYRetornarCategoria_cuandoIdExiste() {
        // Arrange
        var category = Category.builder().id(1).description("Accesorios").build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.findByDescription("Auriculares")).thenReturn(List.of());
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = categoryService.updateCategory(1, "Auriculares");

        // Assert
        assertThat(result.getDescription()).isEqualTo("Auriculares");
    }

    @Test
    void updateCategory_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(99, "Auriculares"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateCategory_deberiaLanzarDuplicateException_cuandoDescripcionUsadaPorOtraCategoria() {
        // Arrange
        var categoryToUpdate = Category.builder().id(2).description("Accesorios").build();
        var otherWithSameName = Category.builder().id(1).description("Smartphones").build();
        when(categoryRepository.findById(2)).thenReturn(Optional.of(categoryToUpdate));
        when(categoryRepository.findByDescription("Smartphones")).thenReturn(List.of(otherWithSameName));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(2, "Smartphones"))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("Smartphones");
    }

    // ─── deactivateCategory ───────────────────────────────────────────────────

    @Test
    void deactivateCategory_deberiaDesactivarCategoria_cuandoIdExiste() {
        // Arrange
        var category = Category.builder().id(1).description("Smartphones").isActive(true).build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = categoryService.deactivateCategory(1);

        // Assert
        assertThat(result.isActive()).isFalse();
        verify(categoryRepository).save(category);
    }

    @Test
    void deactivateCategory_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deactivateCategory(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── deleteCategory ───────────────────────────────────────────────────────

    @Test
    void deleteCategory_deberiaEliminar_cuandoNoTieneProductos() {
        // Arrange
        when(productRepository.existsByCategory_Id(1)).thenReturn(false);

        // Act
        categoryService.deleteCategory(1);

        // Assert
        verify(categoryRepository).deleteById(1);
    }

    @Test
    void deleteCategory_deberiaLanzarBusinessRuleException_cuandoTieneProductosAsociados() {
        // Arrange
        when(productRepository.existsByCategory_Id(1)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("productos asociados");
    }
}
