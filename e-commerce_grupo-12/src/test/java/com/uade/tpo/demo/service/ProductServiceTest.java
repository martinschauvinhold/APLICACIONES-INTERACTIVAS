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
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.ProductRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProducts_deberiaRetornarListaCompleta() {
        // Arrange
        var products = List.of(
                Product.builder().id(1).name("Samsung Galaxy S24").build(),
                Product.builder().id(2).name("iPhone 15").build());
        when(productRepository.findAll()).thenReturn(products);

        // Act
        var result = productService.getProducts();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getProductById_deberiaRetornarProducto_cuandoIdExiste() {
        // Arrange
        var product = Product.builder().id(1).name("Samsung Galaxy S24").brand("Samsung").build();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        // Act
        var result = productService.getProductById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Samsung Galaxy S24");
    }

    @Test
    void getProductById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = productService.getProductById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createProduct_deberiaGuardarYRetornarProducto_cuandoCategoriaExiste() {
        // Arrange
        var category = Category.builder().id(1).description("Smartphones").build();
        var request = new ProductRequest();
        request.setName("Samsung Galaxy S24");
        request.setDescription("Smartphone Android");
        request.setBrand("Samsung");
        request.setCategoryId(1);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = productService.createProduct(request);

        // Assert
        assertThat(result.getName()).isEqualTo("Samsung Galaxy S24");
        assertThat(result.getCategory()).isEqualTo(category);
        verify(productRepository).save(any());
    }

    @Test
    void createProduct_deberiaLanzarNotFoundException_cuandoCategoriaNoExiste() {
        // Arrange
        var request = new ProductRequest();
        request.setName("Producto");
        request.setCategoryId(99);
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateProduct_deberiaActualizarYRetornarProducto_cuandoProductoYCategoriaExisten() {
        // Arrange
        var category = Category.builder().id(2).description("Tablets").build();
        var product = Product.builder().id(1).name("Viejo Nombre").brand("Vieja Marca").build();
        var request = new ProductRequest();
        request.setName("Nuevo Nombre");
        request.setDescription("Nueva Descripción");
        request.setBrand("Nueva Marca");
        request.setCategoryId(2);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = productService.updateProduct(1, request);

        // Assert
        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        assertThat(result.getBrand()).isEqualTo("Nueva Marca");
    }

    @Test
    void updateProduct_deberiaLanzarNotFoundException_cuandoProductoNoExiste() {
        // Arrange
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(99, new ProductRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateProduct_deberiaLanzarNotFoundException_cuandoCategoriaNoExiste() {
        // Arrange
        var product = Product.builder().id(1).name("Producto").build();
        var request = new ProductRequest();
        request.setCategoryId(99);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(1, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteProduct_deberiaEliminar() {
        // Act
        productService.deleteProduct(1);

        // Assert
        verify(productRepository).deleteById(1);
    }
}
