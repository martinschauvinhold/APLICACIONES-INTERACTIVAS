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

import com.uade.tpo.demo.entity.Category;
import com.uade.tpo.demo.entity.Discount;
import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.dto.DiscountRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.CategoryRepository;
import com.uade.tpo.demo.repository.DiscountRepository;
import com.uade.tpo.demo.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DiscountServiceImpl discountService;

    @Test
    void getDiscounts_deberiaRetornarListaCompleta() {
        // Arrange
        var discounts = List.of(
                Discount.builder().id(1).name("10% OFF").build(),
                Discount.builder().id(2).name("$500 OFF").build());
        when(discountRepository.findAll()).thenReturn(discounts);

        // Act
        var result = discountService.getDiscounts();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getDiscountById_deberiaRetornarDescuento_cuandoIdExiste() {
        // Arrange
        var discount = Discount.builder().id(1).name("10% OFF").build();
        when(discountRepository.findById(1)).thenReturn(Optional.of(discount));

        // Act
        var result = discountService.getDiscountById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("10% OFF");
    }

    @Test
    void getDiscountById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(discountRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = discountService.getDiscountById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createDiscount_deberiaGuardarSinProductoNiCategoria() {
        // Arrange
        var request = new DiscountRequest();
        request.setName("10% OFF");
        request.setDiscountType("PERCENTAGE");
        request.setValue(new BigDecimal("10"));
        request.setAppliesTo("PRODUCT");
        when(discountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = discountService.createDiscount(request);

        // Assert
        assertThat(result.getName()).isEqualTo("10% OFF");
        verify(discountRepository).save(any());
    }

    @Test
    void createDiscount_deberiaLanzarNotFoundException_cuandoProductoNoExiste() {
        // Arrange
        var request = new DiscountRequest();
        request.setName("10% OFF");
        request.setProductId(99);
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> discountService.createDiscount(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createDiscount_deberiaLanzarNotFoundException_cuandoCategoriaNoExiste() {
        // Arrange
        var request = new DiscountRequest();
        request.setName("10% OFF");
        request.setCategoryId(99);
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> discountService.createDiscount(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateDiscount_deberiaActualizarYRetornarDescuento_cuandoIdExiste() {
        // Arrange
        var discount = Discount.builder().id(1).name("Viejo").value(new BigDecimal("10")).build();
        var request = new DiscountRequest();
        request.setName("Nuevo");
        request.setValue(new BigDecimal("20"));
        request.setDiscountType("PERCENTAGE");
        request.setAppliesTo("PRODUCT");
        when(discountRepository.findById(1)).thenReturn(Optional.of(discount));
        when(discountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = discountService.updateDiscount(1, request);

        // Assert
        assertThat(result.getName()).isEqualTo("Nuevo");
        assertThat(result.getValue()).isEqualByComparingTo("20");
    }

    @Test
    void updateDiscount_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(discountRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> discountService.updateDiscount(99, new DiscountRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteDiscount_deberiaEliminar_cuandoIdExiste() {
        // Arrange
        when(discountRepository.existsById(1)).thenReturn(true);

        // Act
        discountService.deleteDiscount(1);

        // Assert
        verify(discountRepository).deleteById(1);
    }

    @Test
    void deleteDiscount_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(discountRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> discountService.deleteDiscount(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getActiveDiscountsForProduct_deberiaRetornarDescuentosActivosDelProductoYCategoria() {
        // Arrange
        var category = Category.builder().id(2).description("Smartphones").build();
        var product = Product.builder().id(1).name("Galaxy S24").category(category).build();
        var discountByProduct = Discount.builder().id(1).name("10% directo").isActive(true).build();
        var discountByCategory = Discount.builder().id(2).name("5% categoria").isActive(true).build();

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(discountRepository.findByProductIdAndIsActiveTrue(1)).thenReturn(List.of(discountByProduct));
        when(discountRepository.findByCategoryIdAndIsActiveTrue(2)).thenReturn(List.of(discountByCategory));

        // Act
        var result = discountService.getActiveDiscountsForProduct(1);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getActiveDiscountsForProduct_deberiaLanzarNotFoundException_cuandoProductoNoExiste() {
        // Arrange
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> discountService.getActiveDiscountsForProduct(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
