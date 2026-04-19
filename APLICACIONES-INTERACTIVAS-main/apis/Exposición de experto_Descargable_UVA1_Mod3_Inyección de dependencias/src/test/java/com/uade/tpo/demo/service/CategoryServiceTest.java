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
import com.uade.tpo.demo.exceptions.CategoryDuplicateException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

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

    @Test
    void createCategory_deberiaGuardarYRetornarCategoria_cuandoDescripcionNoDuplicada() throws CategoryDuplicateException {
        // Arrange
        when(categoryRepository.findByDescription("Tablets")).thenReturn(List.of());
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = categoryService.createCategory("Tablets");

        // Assert
        assertThat(result.getDescription()).isEqualTo("Tablets");
        verify(categoryRepository).save(any());
    }

    @Test
    void createCategory_deberiaLanzarCategoryDuplicateException_cuandoDescripcionYaExiste() {
        // Arrange
        var existing = Category.builder().id(1).description("Smartphones").build();
        when(categoryRepository.findByDescription("Smartphones")).thenReturn(List.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory("Smartphones"))
                .isInstanceOf(CategoryDuplicateException.class);
    }

    @Test
    void updateCategory_deberiaActualizarYRetornarCategoria_cuandoIdExiste() {
        // Arrange
        var category = Category.builder().id(1).description("Vieja Descripción").build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = categoryService.updateCategory(1, "Nueva Descripción");

        // Assert
        assertThat(result.getDescription()).isEqualTo("Nueva Descripción");
    }

    @Test
    void updateCategory_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(99, "Descripción"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteCategory_deberiaEliminar() {
        // Act
        categoryService.deleteCategory(1);

        // Assert
        verify(categoryRepository).deleteById(1);
    }
}
