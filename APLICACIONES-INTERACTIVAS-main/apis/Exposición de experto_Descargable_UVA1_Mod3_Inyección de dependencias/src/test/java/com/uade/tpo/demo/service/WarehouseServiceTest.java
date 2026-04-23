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

import com.uade.tpo.demo.entity.Warehouse;
import com.uade.tpo.demo.entity.dto.WarehouseRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    @Test
    void getWarehouses_deberiaRetornarListaCompleta() {
        // Arrange
        var warehouses = List.of(
                Warehouse.builder().id(1).name("Depósito Central").build(),
                Warehouse.builder().id(2).name("Depósito Norte").build());
        when(warehouseRepository.findAll()).thenReturn(warehouses);

        // Act
        var result = warehouseService.getWarehouses();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getWarehouseById_deberiaRetornarDeposito_cuandoIdExiste() {
        // Arrange
        var warehouse = Warehouse.builder().id(1).name("Depósito Central").location("CABA").build();
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));

        // Act
        var result = warehouseService.getWarehouseById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Depósito Central");
    }

    @Test
    void getWarehouseById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = warehouseService.getWarehouseById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void createWarehouse_deberiaGuardarYRetornarDeposito() {
        // Arrange
        var request = new WarehouseRequest();
        request.setName("Depósito Sur");
        request.setLocation("La Plata");
        request.setContactPhone("+54 221 555-1234");

        var saved = Warehouse.builder().id(10).name("Depósito Sur").build();
        when(warehouseRepository.save(any())).thenReturn(saved);

        // Act
        var result = warehouseService.createWarehouse(request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        verify(warehouseRepository).save(any());
    }

    @Test
    void updateWarehouse_deberiaActualizarYRetornarDeposito_cuandoIdExiste() {
        // Arrange
        var warehouse = Warehouse.builder().id(1).name("Viejo Nombre").location("Vieja Loc").build();
        var request = new WarehouseRequest();
        request.setName("Nuevo Nombre");
        request.setLocation("Nueva Loc");
        request.setContactPhone("+54 11 0000-1111");
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = warehouseService.updateWarehouse(1, request);

        // Assert
        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        assertThat(result.getLocation()).isEqualTo("Nueva Loc");
    }

    @Test
    void updateWarehouse_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> warehouseService.updateWarehouse(99, new WarehouseRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteWarehouse_deberiaEliminar() {
        // Act
        warehouseService.deleteWarehouse(1);

        // Assert
        verify(warehouseRepository).deleteById(1);
    }
}
