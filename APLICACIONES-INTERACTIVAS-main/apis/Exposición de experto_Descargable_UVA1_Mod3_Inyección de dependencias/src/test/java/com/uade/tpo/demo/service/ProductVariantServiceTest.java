package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.dto.ProductVariantRequest;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductVariantServiceImpl productVariantService;

    @Test
    void getVariants_deberiaRetornarListaCompleta() {
        // Arrange
        var variants = List.of(
                ProductVariant.builder().id(1).sku("SKU-001").build(),
                ProductVariant.builder().id(2).sku("SKU-002").build());
        when(productVariantRepository.findAll()).thenReturn(variants);

        // Act
        var result = productVariantService.getVariants();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getVariantById_deberiaRetornarVariante_cuandoIdExiste() {
        // Arrange
        var variant = ProductVariant.builder().id(1).sku("SKU-001").build();
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));

        // Act
        var result = productVariantService.getVariantById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo("SKU-001");
    }

    @Test
    void getVariantById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(productVariantRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = productVariantService.getVariantById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getVariantsByProduct_deberiaRetornarVariantesFiltradas() {
        // Arrange
        var product = Product.builder().id(1).build();
        var variants = List.of(
                ProductVariant.builder().id(1).product(product).sku("SKU-001").build(),
                ProductVariant.builder().id(2).product(product).sku("SKU-002").build());
        when(productVariantRepository.findByProductId(1)).thenReturn(variants);

        // Act
        var result = productVariantService.getVariantsByProduct(1);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void createVariant_deberiaGuardarYRetornarVariante_cuandoProductoExiste() {
        // Arrange
        var product = Product.builder().id(1).build();
        var request = new ProductVariantRequest();
        request.setProductId(1);
        request.setSku("SAM-S24-256-BLK");
        request.setAttributes("256GB / Negro");
        request.setBasePrice(new BigDecimal("899.99"));

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productVariantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = productVariantService.createVariant(request);

        // Assert
        assertThat(result.getSku()).isEqualTo("SAM-S24-256-BLK");
        verify(productVariantRepository).save(any());
    }

    @Test
    void createVariant_deberiaLanzarExcepcion_cuandoProductoNoExiste() {
        // Arrange
        var request = new ProductVariantRequest();
        request.setProductId(99);
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productVariantService.createVariant(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateVariant_deberiaActualizarYRetornarVariante_cuandoIdExiste() {
        // Arrange
        var variant = ProductVariant.builder().id(1).sku("SKU-VIEJO").basePrice(new BigDecimal("500")).build();
        var request = new ProductVariantRequest();
        request.setSku("SKU-NUEVO");
        request.setAttributes("512GB / Blanco");
        request.setBasePrice(new BigDecimal("1099.99"));
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(productVariantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = productVariantService.updateVariant(1, request);

        // Assert
        assertThat(result.getSku()).isEqualTo("SKU-NUEVO");
        assertThat(result.getBasePrice()).isEqualByComparingTo("1099.99");
    }

    @Test
    void updateVariant_deberiaLanzarExcepcion_cuandoIdNoExiste() {
        // Arrange
        when(productVariantRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productVariantService.updateVariant(99, new ProductVariantRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteVariant_deberiaEliminar() {
        // Act
        productVariantService.deleteVariant(1);

        // Assert
        verify(productVariantRepository).deleteById(1);
    }
}
