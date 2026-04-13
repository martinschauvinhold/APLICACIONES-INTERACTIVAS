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
import com.uade.tpo.demo.entity.ShipmentTracking;
import com.uade.tpo.demo.entity.TrackingStatus;
import com.uade.tpo.demo.entity.dto.ShipmentTrackingRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.DeliveryRepository;
import com.uade.tpo.demo.repository.ShipmentTrackingRepository;

@ExtendWith(MockitoExtension.class)
class ShipmentTrackingServiceTest {

    @Mock
    private ShipmentTrackingRepository trackingRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private ShipmentTrackingServiceImpl trackingService;

    @Test
    void getByDeliveryId_deberiaRetornarCheckpoints_cuandoDeliveryExiste() {
        // Arrange
        var delivery = Delivery.builder().id(1).build();
        var checkpoints = List.of(
                ShipmentTracking.builder().id(1).delivery(delivery).checkpoint("En deposito").build(),
                ShipmentTracking.builder().id(2).delivery(delivery).checkpoint("En camino").build());
        when(deliveryRepository.existsById(1)).thenReturn(true);
        when(trackingRepository.findByDeliveryId(1)).thenReturn(checkpoints);

        // Act
        var result = trackingService.getByDeliveryId(1);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCheckpoint()).isEqualTo("En deposito");
    }

    @Test
    void getByDeliveryId_deberiaLanzarNotFoundException_cuandoDeliveryNoExiste() {
        // Arrange
        when(deliveryRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> trackingService.getByDeliveryId(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Delivery")
                .hasMessageContaining("99");
    }

    @Test
    void getById_deberiaRetornarCheckpoint_cuandoIdExiste() {
        // Arrange
        var checkpoint = ShipmentTracking.builder().id(1).checkpoint("En deposito").status(TrackingStatus.IN_TRANSIT).build();
        when(trackingRepository.findById(1)).thenReturn(Optional.of(checkpoint));

        // Act
        var result = trackingService.getById(1);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCheckpoint()).isEqualTo("En deposito");
    }

    @Test
    void getById_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(trackingRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> trackingService.getById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addCheckpoint_deberiaGuardarYRetornarCheckpoint_cuandoDeliveryExiste() {
        // Arrange
        var delivery = Delivery.builder().id(1).build();
        var request = new ShipmentTrackingRequest(1, "En deposito central", TrackingStatus.IN_TRANSIT);
        var guardado = ShipmentTracking.builder().id(10).delivery(delivery)
                .checkpoint("En deposito central").status(TrackingStatus.IN_TRANSIT).build();
        when(deliveryRepository.findById(1)).thenReturn(Optional.of(delivery));
        when(trackingRepository.save(any())).thenReturn(guardado);

        // Act
        var result = trackingService.addCheckpoint(request);

        // Assert
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getCheckpoint()).isEqualTo("En deposito central");
        verify(trackingRepository).save(any());
    }

    @Test
    void addCheckpoint_deberiaLanzarNotFoundException_cuandoDeliveryNoExiste() {
        // Arrange
        var request = new ShipmentTrackingRequest(99, "En deposito", TrackingStatus.IN_TRANSIT);
        when(deliveryRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> trackingService.addCheckpoint(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Delivery")
                .hasMessageContaining("99");
    }

    @Test
    void updateStatus_deberiaActualizarStatus_cuandoIdExiste() {
        // Arrange
        var checkpoint = ShipmentTracking.builder().id(1).status(TrackingStatus.IN_TRANSIT).build();
        when(trackingRepository.findById(1)).thenReturn(Optional.of(checkpoint));
        when(trackingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = trackingService.updateStatus(1, TrackingStatus.DELIVERED);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TrackingStatus.DELIVERED);
        verify(trackingRepository).save(any());
    }

    @Test
    void updateStatus_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(trackingRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> trackingService.updateStatus(99, TrackingStatus.DELIVERED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
