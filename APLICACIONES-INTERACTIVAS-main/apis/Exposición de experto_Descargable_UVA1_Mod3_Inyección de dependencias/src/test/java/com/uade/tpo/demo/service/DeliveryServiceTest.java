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
import com.uade.tpo.demo.entity.dto.DeliveryRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    @Test
    void getDeliveries_deberiaRetornarListaCompleta() {
        // Arrange
        var deliveries = List.of(
                Delivery.builder().id(1).shippingMethod("correo").build(),
                Delivery.builder().id(2).shippingMethod("moto").build());
        when(deliveryRepository.findAll()).thenReturn(deliveries);

        // Act
        var result = deliveryService.getDeliveries();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getDeliveryById_deberiaRetornarDelivery_cuandoIdExiste() {
        // Arrange
        var delivery = Delivery.builder().id(1).shippingMethod("correo").build();
        when(deliveryRepository.findById(1)).thenReturn(Optional.of(delivery));

        // Act
        var result = deliveryService.getDeliveryById(1);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getShippingMethod()).isEqualTo("correo");
    }

    @Test
    void getDeliveryById_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(deliveryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deliveryService.getDeliveryById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createDelivery_deberiaGuardarYRetornarDelivery_cuandoOrderExiste() {
        // Arrange
        var order = Order.builder().id(1).build();
        var request = new DeliveryRequest(1, "correo", "TRK-001", DeliveryStatus.PENDING);
        var deliveryGuardado = Delivery.builder().id(10).order(order).shippingMethod("correo").build();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(deliveryRepository.save(any())).thenReturn(deliveryGuardado);

        // Act
        var result = deliveryService.createDelivery(request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        verify(deliveryRepository).save(any());
    }

    @Test
    void createDelivery_deberiaLanzarNotFoundException_cuandoOrderNoExiste() {
        // Arrange
        var request = new DeliveryRequest(99, "correo", "TRK-001", DeliveryStatus.PENDING);
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deliveryService.createDelivery(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order")
                .hasMessageContaining("99");
    }

    @Test
    void updateDelivery_deberiaActualizarYRetornarDelivery_cuandoIdExiste() {
        // Arrange
        var delivery = Delivery.builder().id(1).shippingMethod("correo").trackingNumber("OLD").build();
        var request = new DeliveryRequest(1, "moto", "TRK-999", DeliveryStatus.DISPATCHED);
        when(deliveryRepository.findById(1)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = deliveryService.updateDelivery(1, request);

        // Assert
        assertThat(result.getShippingMethod()).isEqualTo("moto");
        assertThat(result.getTrackingNumber()).isEqualTo("TRK-999");
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DISPATCHED);
    }

    @Test
    void updateDelivery_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        var request = new DeliveryRequest(1, "moto", "TRK-999", DeliveryStatus.DISPATCHED);
        when(deliveryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deliveryService.updateDelivery(99, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteDelivery_deberiaEliminar_cuandoIdExiste() {
        // Arrange
        when(deliveryRepository.existsById(1)).thenReturn(true);

        // Act
        deliveryService.deleteDelivery(1);

        // Assert
        verify(deliveryRepository).deleteById(1);
    }

    @Test
    void deleteDelivery_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(deliveryRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> deliveryService.deleteDelivery(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
