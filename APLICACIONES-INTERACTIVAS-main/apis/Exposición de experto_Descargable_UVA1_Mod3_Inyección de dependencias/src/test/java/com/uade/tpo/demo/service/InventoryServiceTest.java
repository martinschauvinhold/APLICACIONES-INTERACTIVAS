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

import com.uade.tpo.demo.entity.Inventory;
import com.uade.tpo.demo.entity.ProductVariant;
import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.entity.dto.InventoryRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.InventoryRepository;
import com.uade.tpo.demo.repository.ProductVariantRepository;
import com.uade.tpo.demo.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void getInventory_deberiaRetornarListaCompleta() {
        // Arrange
        var items = List.of(
                Inventory.builder().id(1).stockQuantity(50).build(),
                Inventory.builder().id(2).stockQuantity(30).build());
        when(inventoryRepository.findAll()).thenReturn(items);

        // Act
        var result = inventoryService.getInventory();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getInventoryById_deberiaRetornarInventario_cuandoIdExiste() {
        // Arrange
        var item = Inventory.builder().id(1).stockQuantity(50).build();
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(item));

        // Act
        var result = inventoryService.getInventoryById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStockQuantity()).isEqualTo(50);
    }

    @Test
    void getInventoryById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(inventoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = inventoryService.getInventoryById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getInventoryByVariant_deberiaRetornarListaFiltrada() {
        // Arrange
        var variant = ProductVariant.builder().id(3).build();
        var items = List.of(Inventory.builder().id(1).variant(variant).stockQuantity(100).build());
        when(productVariantRepository.existsById(3)).thenReturn(true);
        when(inventoryRepository.findByVariantId(3)).thenReturn(items);

        // Act
        var result = inventoryService.getInventoryByVariant(3);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getInventoryByVariant_deberiaLanzarNotFoundException_cuandoVariantNoExiste() {
        // Arrange
        when(productVariantRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.getInventoryByVariant(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createInventory_deberiaGuardarYRetornarInventario_cuandoVariantYWarehouseExisten() {
        // Arrange
        var variant = ProductVariant.builder().id(1).build();
        var warehouse = Warehouse.builder().id(2).build();
        var request = new InventoryRequest();
        request.setVariantId(1);
        request.setWarehouseId(2);
        request.setStockQuantity(50);

        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(warehouseRepository.findById(2)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = inventoryService.createInventory(request);

        // Assert
        assertThat(result.getStockQuantity()).isEqualTo(50);
        verify(inventoryRepository).save(any());
    }

    @Test
    void createInventory_deberiaLanzarNotFoundException_cuandoVariantNoExiste() {
        // Arrange
        var request = new InventoryRequest();
        request.setVariantId(99);
        request.setWarehouseId(1);
        when(productVariantRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createInventory_deberiaLanzarNotFoundException_cuandoWarehouseNoExiste() {
        // Arrange
        var variant = ProductVariant.builder().id(1).build();
        var request = new InventoryRequest();
        request.setVariantId(1);
        request.setWarehouseId(99);
        when(productVariantRepository.findById(1)).thenReturn(Optional.of(variant));
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.createInventory(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateInventory_deberiaActualizarStockYRetornar_cuandoIdExiste() {
        // Arrange
        var item = Inventory.builder().id(1).stockQuantity(50).build();
        var request = new InventoryRequest();
        request.setStockQuantity(75);
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = inventoryService.updateInventory(1, request);

        // Assert
        assertThat(result.getStockQuantity()).isEqualTo(75);
    }

    @Test
    void updateInventory_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(inventoryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.updateInventory(99, new InventoryRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteInventory_deberiaEliminar() {
        // Act
        inventoryService.deleteInventory(1);

        // Assert
        verify(inventoryRepository).deleteById(1);
    }
}
