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

import com.uade.tpo.demo.entity.Delivery;
import com.uade.tpo.demo.entity.DeliveryStatus;
import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.ProductReturn;
import com.uade.tpo.demo.entity.ReturnStatus;
import com.uade.tpo.demo.entity.dto.ProductReturnRequest;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.OrderRepository;
import com.uade.tpo.demo.repository.ProductReturnRepository;

@ExtendWith(MockitoExtension.class)
class ProductReturnServiceTest {

    @Mock
    private ProductReturnRepository returnRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private ProductReturnServiceImpl returnService;

    @Test
    void getReturns_deberiaRetornarListaCompleta() {
        // Arrange
        var returns = List.of(
                ProductReturn.builder().id(1).reason("Pantalla rota").build(),
                ProductReturn.builder().id(2).reason("Batería defectuosa").build());
        when(returnRepository.findAll()).thenReturn(returns);

        // Act
        var result = returnService.getReturns();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getReturnById_deberiaRetornarReturn_cuandoIdExiste() {
        // Arrange
        var productReturn = ProductReturn.builder().id(1).reason("Pantalla rota").status(ReturnStatus.PENDING).build();
        when(returnRepository.findById(1)).thenReturn(Optional.of(productReturn));

        // Act
        var result = returnService.getReturnById(1);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getReason()).isEqualTo("Pantalla rota");
    }

    @Test
    void getReturnById_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(returnRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> returnService.getReturnById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createReturn_deberiaGuardarReturn_cuandoOrderTieneDeliveryDespachado() {
        // Arrange
        var order = Order.builder().id(1).build();
        var delivery = Delivery.builder().id(1).order(order).status(DeliveryStatus.DISPATCHED).build();
        var request = new ProductReturnRequest(1, "Pantalla rota", ReturnStatus.PENDING);
        var returnGuardado = ProductReturn.builder().id(10).order(order).reason("Pantalla rota").build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(List.of(delivery));
        when(returnRepository.save(any())).thenReturn(returnGuardado);

        // Act
        var result = returnService.createReturn(request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        verify(returnRepository).save(any());
    }

    @Test
    void createReturn_deberiaLanzarNotFoundException_cuandoOrderNoExiste() {
        // Arrange
        var request = new ProductReturnRequest(99, "Pantalla rota", ReturnStatus.PENDING);
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> returnService.createReturn(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order")
                .hasMessageContaining("99");
    }

    @Test
    void createReturn_deberiaLanzarBusinessRuleException_cuandoDeliveryNoDespachado() {
        // Arrange
        var order = Order.builder().id(1).build();
        var pendingDelivery = Delivery.builder().id(1).order(order).status(DeliveryStatus.PENDING).build();
        var request = new ProductReturnRequest(1, "Pantalla rota", ReturnStatus.PENDING);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(List.of(pendingDelivery));

        // Act & Assert
        assertThatThrownBy(() -> returnService.createReturn(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dispatched");
    }

    @Test
    void createReturn_deberiaLanzarBusinessRuleException_cuandoNoHayDelivery() {
        // Arrange
        var order = Order.builder().id(1).build();
        var request = new ProductReturnRequest(1, "Pantalla rota", ReturnStatus.PENDING);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.findByOrderId(1)).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> returnService.createReturn(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dispatched");
    }

    @Test
    void updateReturn_deberiaActualizarReturn_cuandoIdExiste() {
        // Arrange
        var productReturn = ProductReturn.builder().id(1).reason("Batería defectuosa").status(ReturnStatus.PENDING).build();
        var request = new ProductReturnRequest(1, "Pantalla rota en transporte", ReturnStatus.APPROVED);
        when(returnRepository.findById(1)).thenReturn(Optional.of(productReturn));
        when(returnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = returnService.updateReturn(1, request);

        // Assert
        assertThat(result.getReason()).isEqualTo("Pantalla rota en transporte");
        assertThat(result.getStatus()).isEqualTo(ReturnStatus.APPROVED);
    }

    @Test
    void updateReturn_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        var request = new ProductReturnRequest(1, "Pantalla rota", ReturnStatus.APPROVED);
        when(returnRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> returnService.updateReturn(99, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteReturn_deberiaEliminar_cuandoIdExiste() {
        // Arrange
        when(returnRepository.existsById(1)).thenReturn(true);

        // Act
        returnService.deleteReturn(1);

        // Assert
        verify(returnRepository).deleteById(1);
    }

    @Test
    void deleteReturn_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(returnRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> returnService.deleteReturn(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
