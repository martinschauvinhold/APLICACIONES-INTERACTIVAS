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

import com.uade.tpo.demo.entity.Order;
import com.uade.tpo.demo.entity.OrderItem;
import com.uade.tpo.demo.entity.OrderStatus;
import com.uade.tpo.demo.exceptions.BusinessRuleException;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.OrderItemRepository;
import com.uade.tpo.demo.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void getItemsByOrder_deberiaRetornarItemsFiltrados() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).build();
        var items = List.of(
                OrderItem.builder().id(1).order(order).quantity(2).build(),
                OrderItem.builder().id(2).order(order).quantity(1).build());
        when(orderRepository.existsById(1)).thenReturn(true);
        when(orderItemRepository.findByOrderId(1)).thenReturn(items);

        // Act
        var result = orderItemService.getItemsByOrder(1);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getItemsByOrder_deberiaLanzarNotFoundException_cuandoOrdenNoExiste() {
        // Arrange
        when(orderRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> orderItemService.getItemsByOrder(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getItemById_deberiaRetornarItem_cuandoIdExiste() {
        // Arrange
        var item = OrderItem.builder().id(1).quantity(3).build();
        when(orderItemRepository.findById(1)).thenReturn(Optional.of(item));

        // Act
        var result = orderItemService.getItemById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(3);
    }

    @Test
    void getItemById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(orderItemRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = orderItemService.getItemById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void deleteItem_deberiaEliminarItem_cuandoOrdenEnPendingYNoEsUltimo() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).build();
        var item1 = OrderItem.builder().id(1).order(order).subtotal(BigDecimal.TEN).build();
        var item2 = OrderItem.builder().id(2).order(order).subtotal(BigDecimal.TEN).build();
        when(orderItemRepository.findById(1)).thenReturn(Optional.of(item1));
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(item1, item2));
        when(orderRepository.save(any())).thenReturn(order);

        // Act
        orderItemService.deleteItem(1);

        // Assert
        verify(orderItemRepository).deleteById(1);
        verify(orderRepository).save(any());
    }

    @Test
    void deleteItem_deberiaLanzarNotFoundException_cuandoItemNoExiste() {
        // Arrange
        when(orderItemRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderItemService.deleteItem(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteItem_deberiaLanzarBusinessRuleException_cuandoOrdenNoEstaPending() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PAID).build();
        var item = OrderItem.builder().id(1).order(order).build();
        when(orderItemRepository.findById(1)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThatThrownBy(() -> orderItemService.deleteItem(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void deleteItem_deberiaLanzarBusinessRuleException_cuandoEsElUltimoItem() {
        // Arrange
        var order = Order.builder().id(1).status(OrderStatus.PENDING).build();
        var item = OrderItem.builder().id(1).order(order).subtotal(BigDecimal.TEN).build();
        when(orderItemRepository.findById(1)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(1)).thenReturn(List.of(item));

        // Act & Assert
        assertThatThrownBy(() -> orderItemService.deleteItem(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ultimo item");
    }
}
